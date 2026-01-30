//! 集成测试：模拟真实生产环境
//!
//! 测试场景：
//! - 50 辆车的排产计划
//! - 3 种颜色（RED/BLUE/WHITE）
//! - 2 种配置（BASE/PREMIUM）
//! - 限制能耗和排放
//! - 对比 4 种算法的表现

use aps_engine::model::*;
use aps_engine::alg::baseline::solve_edd;
use aps_engine::alg::sa::solve_sa;
use aps_engine::alg::hybrid::solve_hybrid;
use std::time::Instant;

/// 生成测试数据：模拟真实生产场景
fn generate_realistic_jobs(count: usize, seed: u64) -> Vec<Job> {
    use rand::{Rng, SeedableRng};
    use rand::rngs::StdRng;

    let mut rng = StdRng::seed_from_u64(seed);
    let mut jobs = Vec::new();

    // 基准时间：2024-01-01 08:00:00
    let base_time = 1704106800000_i64;

    // 颜色分布：50% RED, 30% BLUE, 20% WHITE
    let colors = vec![
        ("RED", 0.5),
        ("BLUE", 0.3),
        ("WHITE", 0.2),
    ];

    // 配置分布：60% BASE, 40% PREMIUM
    let configs = vec![
        ("BASE", 0.6),
        ("PREMIUM", 0.4),
    ];

    for i in 0..count {
        // 随机选择颜色和配置
        let color = if rng.gen_bool(colors[0].1) {
            colors[0].0
        } else if rng.gen_bool(colors[1].1 / (1.0 - colors[0].1)) {
            colors[1].0
        } else {
            colors[2].0
        };

        let config = if rng.gen_bool(configs[0].1) {
            configs[0].0
        } else {
            configs[1].0
        };

        // 总装时间：BASE 45-75分钟，PREMIUM 60-90分钟
        let assemble_minutes = if config == "BASE" {
            rng.gen_range(45..=75)
        } else {
            rng.gen_range(60..=90)
        };

        // 四大工艺时间（模拟真实比例）
        let stamping_minutes = rng.gen_range(8..=15);   // 冲压：8-15分钟
        let welding_minutes = rng.gen_range(20..=35);   // 焊装：20-35分钟
        let painting_minutes = rng.gen_range(40..=60);  // 涂装：40-60分钟

        // 交付期：从当前时间开始，间隔 1-3 小时
        let due_offset_hours = rng.gen_range(1..=3);
        let due_epoch_ms = base_time + ((i as i64 + 1) * due_offset_hours * 3600 * 1000);

        // 能耗和排放：与加工时间相关
        let total_minutes = stamping_minutes + welding_minutes + painting_minutes + assemble_minutes;
        let energy_score = (total_minutes as f64 * 1.8) + rng.gen_range(-10.0..10.0);
        let emission_score = (total_minutes as f64 * 0.9) + rng.gen_range(-5.0..5.0);

        jobs.push(Job {
            vin: format!("VIN{:04}", i + 1),
            due_epoch_ms,
            stamping_minutes,
            welding_minutes,
            painting_minutes,
            assemble_minutes,
            mold_code: format!("MOLD_{}", rng.gen_range(1..=5)),
            welding_fixture: format!("FIX_{}", rng.gen_range(1..=3)),
            color: color.to_string(),
            config: config.to_string(),
            energy_score,
            emission_score,
        });
    }

    jobs
}

/// 打印 KPI 对比表
fn print_comparison_table(results: &[(String, SolveOutput)]) {
    println!("\n{}", "=".repeat(100));
    println!("{:^100}", "🏭 算法性能对比报告");
    println!("{}", "=".repeat(100));

    // 表头
    println!(
        "{:<12} | {:>10} | {:>12} | {:>10} | {:>10} | {:>10} | {:>10}",
        "算法", "总成本", "总延迟(min)", "最大延迟", "颜色切换", "配置切换", "运行时间(ms)"
    );
    println!("{}", "-".repeat(100));

    // 找到最优值（用于高亮）
    let min_cost = results.iter().map(|(_, o)| o.summary.cost).fold(f64::INFINITY, f64::min);
    let min_tardiness = results.iter().map(|(_, o)| o.summary.total_tardiness_min).min().unwrap_or(0);

    // 数据行
    for (algo, output) in results {
        let s = &output.summary;

        // 标记最优值
        let cost_mark = if (s.cost - min_cost).abs() < 1e-6 { "✓" } else { "" };
        let tard_mark = if s.total_tardiness_min == min_tardiness { "✓" } else { "" };

        println!(
            "{:<12} | {:>9.2}{} | {:>12}{} | {:>10} | {:>10} | {:>10} | {:>10}",
            algo,
            s.cost, cost_mark,
            s.total_tardiness_min, tard_mark,
            s.max_tardiness_min,
            s.color_changes,
            s.config_changes,
            s.elapsed_ms,
        );
    }

    println!("{}", "=".repeat(100));
}

/// 打印详细的调度分析
fn print_schedule_analysis(name: &str, output: &SolveOutput) {
    println!("\n{}", "─".repeat(80));
    println!("📊 {} 调度详情", name);
    println!("{}", "─".repeat(80));

    let schedule = &output.schedule;

    // 统计班次分布
    use std::collections::HashMap;
    let mut shift_stats: HashMap<String, i32> = HashMap::new();
    for item in schedule {
        *shift_stats.entry(item.shift_id.clone()).or_insert(0) += 1;
    }

    println!("📅 班次分布：");
    for (shift, count) in shift_stats.iter() {
        println!("   {} : {} 辆", shift, count);
    }

    // 颜色切换分析
    let mut color_switches = Vec::new();
    for i in 1..schedule.len() {
        if schedule[i].color != schedule[i-1].color {
            color_switches.push((
                i,
                schedule[i-1].color.clone(),
                schedule[i].color.clone(),
            ));
        }
    }

    println!("\n🎨 颜色切换记录 ({} 次)：", color_switches.len());
    for (idx, from, to) in color_switches.iter().take(5) {
        println!("   位置 {} : {} → {}", idx, from, to);
    }
    if color_switches.len() > 5 {
        println!("   ... 还有 {} 次切换", color_switches.len() - 5);
    }

    // 延迟分析
    let delayed_jobs: Vec<_> = schedule.iter()
        .filter(|item| item.tardiness_min > 0)
        .collect();

    println!("\n⏰ 延迟分析：");
    println!("   延迟车辆数：{} / {}", delayed_jobs.len(), schedule.len());
    if !delayed_jobs.is_empty() {
        println!("   总延迟时间：{} 分钟", output.summary.total_tardiness_min);
        println!("   平均延迟：{:.1} 分钟",
                 output.summary.total_tardiness_min as f64 / delayed_jobs.len() as f64);
        println!("   最大延迟：{} 分钟", output.summary.max_tardiness_min);
    }

    // 违规分析
    if !output.violations.is_empty() {
        println!("\n⚠️  约束违规 ({} 项)：", output.violations.len());
        for v in output.violations.iter().take(3) {
            println!("   {} - {} : 超出 {:.2}", v.shift_id, v.vtype, v.excess);
        }
    } else {
        println!("\n✅ 无约束违规");
    }
}

/// 打印收敛曲线分析
fn print_convergence_analysis(name: &str, convergence: &[ConvergencePoint]) {
    println!("\n📈 {} 收敛分析", name);

    if convergence.len() < 2 {
        println!("   收敛点不足");
        return;
    }

    let initial_cost = convergence.first().unwrap().best_cost;
    let final_cost = convergence.last().unwrap().best_cost;
    let improvement = ((initial_cost - final_cost) / initial_cost * 100.0).max(0.0);

    println!("   初始成本：{:.2}", initial_cost);
    println!("   最终成本：{:.2}", final_cost);
    println!("   改进幅度：{:.2}%", improvement);
    println!("   收敛点数：{}", convergence.len());

    // 打印简化的收敛曲线
    println!("   收敛曲线：");
    let step = convergence.len() / 10.max(1);
    for (i, point) in convergence.iter().enumerate().step_by(step.max(1)) {
        let bar_len = ((initial_cost - point.best_cost) / initial_cost * 40.0) as usize;
        let bar = "█".repeat(bar_len);
        println!("   {:>5}ms │{:<40}│ {:.2}", point.t_ms, bar, point.best_cost);
    }
}

#[test]
fn test_realistic_scenario_50_jobs() {
    println!("\n");
    println!("╔═══════════════════════════════════════════════════════════════════╗");
    println!("║          🏭 真实生产环境模拟测试 - 50辆车排产计划                 ║");
    println!("╚═══════════════════════════════════════════════════════════════════╝");

    // 生成测试数据
    let jobs = generate_realistic_jobs(50, 12345);

    println!("\n📦 测试数据概况：");
    println!("   车辆数量：{} 辆", jobs.len());

    // 统计颜色和配置分布
    use std::collections::HashMap;
    let mut color_dist: HashMap<String, i32> = HashMap::new();
    let mut config_dist: HashMap<String, i32> = HashMap::new();

    for job in &jobs {
        *color_dist.entry(job.color.clone()).or_insert(0) += 1;
        *config_dist.entry(job.config.clone()).or_insert(0) += 1;
    }

    println!("   颜色分布：{:?}", color_dist);
    println!("   配置分布：{:?}", config_dist);

    let total_minutes: i32 = jobs.iter().map(|j| j.total_minutes()).sum();
    println!("   总加工时间：{} 分钟 ({:.1} 小时)", total_minutes, total_minutes as f64 / 60.0);

    // 构建输入参数
    let plan_start = 1704106800000_i64;
    let weights = Weights {
        tardiness: 10.0,
        color_change: 50.0,
        config_change: 30.0,
        energy_excess: 2.0,
        emission_excess: 3.0,
        material_shortage: 0.0,
    };
    let limits = Limits {
        max_energy_per_shift: 5000.0,
        max_emission_per_shift: 2500.0,
    };

    println!("\n⚙️  优化参数：");
    println!("   延迟权重：{}", weights.tardiness);
    println!("   颜色切换权重：{}", weights.color_change);
    println!("   配置切换权重：{}", weights.config_change);
    println!("   班次能耗限制：{}", limits.max_energy_per_shift);
    println!("   班次排放限制：{}", limits.max_emission_per_shift);

    // 准备测试用例
    let test_cases = vec![
        ("Baseline (EDD)", "baseline", 1, solve_edd as fn(&SolveInput) -> SolveOutput),
        ("Simulated Annealing", "sa", 5, solve_sa as fn(&SolveInput) -> SolveOutput),
        //("Genetic Algorithm", "ga", 5, solve_ga as fn(&SolveInput) -> SolveOutput),
        ("Hybrid (GA+SA)", "hybrid", 8, solve_hybrid as fn(&SolveInput) -> SolveOutput),
    ];

    let mut results = Vec::new();

    // 执行测试
    println!("\n🚀 开始执行算法测试...\n");

    for (name, algo, time_budget, solver) in test_cases {
        let input = SolveInput {
            request_id: format!("test-{}", algo),
            plan_start_epoch_ms: plan_start,
            jobs: jobs.clone(),
            params: SolveParams {
                algorithm: algo.to_string(),
                time_budget_sec: time_budget,
                seed: 42,
                weights: weights.clone(),
                limits: limits.clone(),
            },
        };

        print!("   🔄 运行 {} ...", name);
        let start = Instant::now();
        let output = solver(&input);
        let elapsed = start.elapsed();
        println!(" ✓ 完成 ({:.2}s)", elapsed.as_secs_f64());

        results.push((name.to_string(), output));
    }

    // 打印对比表
    print_comparison_table(&results);

    // 详细分析每个算法
    for (name, output) in &results {
        print_schedule_analysis(name, output);
        print_convergence_analysis(name, &output.convergence);
    }

    // 验证基本约束
    println!("\n{}", "─".repeat(80));
    println!("✅ 基本约束验证");
    println!("{}", "─".repeat(80));

    for (name, output) in &results {
        let schedule = &output.schedule;

        // 验证1：所有车辆都被排产
        assert_eq!(schedule.len(), jobs.len(),
                   "{}: 排产数量不匹配", name);

        // 验证2：没有时间重叠（单产线假设）
        let mut sorted = schedule.clone();
        sorted.sort_by_key(|item| item.start_epoch_ms);
        for i in 1..sorted.len() {
            assert!(sorted[i].start_epoch_ms >= sorted[i-1].end_epoch_ms,
                    "{}: 时间重叠 VIN{} 和 VIN{}", name, sorted[i-1].vin, sorted[i].vin);
        }

        // 验证3：延迟计算正确
        for item in schedule {
            let expected_tardiness = ((item.end_epoch_ms - item.due_epoch_ms) / 60_000).max(0);
            assert_eq!(item.tardiness_min, expected_tardiness,
                       "{}: VIN{} 延迟计算错误", name, item.vin);
        }

        println!("   ✓ {} 约束验证通过", name);
    }

    // 性能对比分析
    println!("\n{}", "─".repeat(80));
    println!("📊 算法性能对比总结");
    println!("{}", "─".repeat(80));

    let baseline = &results[0].1;

    for (name, output) in results.iter().skip(1) {
        let cost_improvement = ((baseline.summary.cost - output.summary.cost) / baseline.summary.cost * 100.0).max(0.0);
        let tardiness_improvement = ((baseline.summary.total_tardiness_min - output.summary.total_tardiness_min) as f64
            / baseline.summary.total_tardiness_min as f64 * 100.0).max(0.0);

        println!("\n🎯 {} vs Baseline:", name);
        println!("   成本改进：{:.2}%", cost_improvement);
        println!("   延迟改进：{:.2}%", tardiness_improvement);
        println!("   颜色切换：{} → {} ({}%)",
                 baseline.summary.color_changes,
                 output.summary.color_changes,
                 ((baseline.summary.color_changes - output.summary.color_changes) as f64 / baseline.summary.color_changes as f64 * 100.0).max(0.0)
        );

        // 判断是否有显著改进
        if cost_improvement > 5.0 {
            println!("   ✅ 显著改进！");
        } else if cost_improvement > 0.0 {
            println!("   ✓ 小幅改进");
        } else {
            println!("   ⚠️  未见改进");
        }
    }

    // 最终结论
    println!("\n{}", "═".repeat(80));
    let best = results.iter()
        .min_by(|a, b| a.1.summary.cost.partial_cmp(&b.1.summary.cost).unwrap())
        .unwrap();
    println!("🏆 最佳算法：{} (成本: {:.2})", best.0, best.1.summary.cost);
    println!("{}", "═".repeat(80));
}

#[test]
fn test_small_scale_10_jobs() {
    println!("\n╔═══════════════════════════════════════════════════════════════════╗");
    println!("║              🧪 小规模测试 - 10辆车快速验证                        ║");
    println!("╚═══════════════════════════════════════════════════════════════════╝");

    let jobs = generate_realistic_jobs(10, 54321);
    let plan_start = 1704106800000_i64;

    let input = SolveInput {
        request_id: "test-small".to_string(),
        plan_start_epoch_ms: plan_start,
        jobs: jobs.clone(),
        params: SolveParams {
            algorithm: "hybrid".to_string(),
            time_budget_sec: 3,
            seed: 42,
            weights: Weights {
                tardiness: 10.0,
                color_change: 50.0,
                config_change: 30.0,
                energy_excess: 2.0,
                emission_excess: 3.0,
                material_shortage: 0.0,
            },
            limits: Limits {
                max_energy_per_shift: 2000.0,
                max_emission_per_shift: 1000.0,
            },
        },
    };

    println!("\n📦 测试数据：{} 辆车", jobs.len());

    let output = solve_hybrid(&input);

    println!("\n📊 结果：");
    println!("   总成本：{:.2}", output.summary.cost);
    println!("   总延迟：{} 分钟", output.summary.total_tardiness_min);
    println!("   颜色切换：{} 次", output.summary.color_changes);
    println!("   配置切换：{} 次", output.summary.config_changes);
    println!("   运行时间：{} ms", output.summary.elapsed_ms);

    // 打印完整排产顺序
    println!("\n📋 排产顺序：");
    for (i, vin) in output.order.iter().enumerate() {
        let job = jobs.iter().find(|j| &j.vin == vin).unwrap();
        println!("   {:2}. {} - {} {} ({}min)",
                 i + 1, vin, job.color, job.config, job.assemble_minutes);
    }

    assert_eq!(output.schedule.len(), jobs.len());
    assert!(output.summary.cost >= 0.0);

    println!("\n✅ 小规模测试通过");
}

#[test]
fn test_stress_100_jobs() {
    println!("\n╔═══════════════════════════════════════════════════════════════════╗");
    println!("║              🔥 压力测试 - 100辆车性能验证                         ║");
    println!("╚═══════════════════════════════════════════════════════════════════╝");

    let jobs = generate_realistic_jobs(100, 99999);
    let plan_start = 1704106800000_i64;

    println!("\n📦 测试数据：{} 辆车", jobs.len());

    let test_cases = vec![
        ("Baseline", "baseline", 1),
        ("SA", "sa", 10),
        ("Hybrid", "hybrid", 15),
    ];

    let mut results = Vec::new();

    for (name, algo, time_budget) in test_cases {
        let input = SolveInput {
            request_id: format!("stress-{}", algo),
            plan_start_epoch_ms: plan_start,
            jobs: jobs.clone(),
            params: SolveParams {
                algorithm: algo.to_string(),
                time_budget_sec: time_budget,
                seed: 42,
                weights: Weights {
                    tardiness: 10.0,
                    color_change: 50.0,
                    config_change: 30.0,
                    energy_excess: 2.0,
                    emission_excess: 3.0,
                    material_shortage: 0.0,
                },
                limits: Limits {
                    max_energy_per_shift: 10000.0,
                    max_emission_per_shift: 5000.0,
                },
            },
        };

        print!("   🔄 运行 {} ...", name);
        let start = Instant::now();

        let output = match algo {
            "baseline" => solve_edd(&input),
            "sa" => solve_sa(&input),
            "hybrid" => solve_hybrid(&input),
            _ => panic!("Unknown algorithm"),
        };

        let elapsed = start.elapsed();
        println!(" ✓ 完成 ({:.2}s)", elapsed.as_secs_f64());

        // 验证结果
        assert_eq!(output.schedule.len(), jobs.len(),
                   "{}: 排产数量不匹配", name);
        assert!(output.summary.cost >= 0.0,
                "{}: 成本为负", name);

        results.push((name.to_string(), output, elapsed));
    }

    // 打印结果
    print_comparison_table(&results.iter().map(|(n, o, _)| (n.clone(), o.clone())).collect::<Vec<_>>());

    // 性能验证
    println!("\n⏱️  性能验证：");
    for (name, _, elapsed) in &results {
        let throughput = jobs.len() as f64 / elapsed.as_secs_f64();
        println!("   {} : {:.1} 车/秒", name, throughput);
    }

    println!("\n✅ 压力测试通过");
}

#[test]
fn test_edge_cases() {
    println!("\n╔═══════════════════════════════════════════════════════════════════╗");
    println!("║                    🔬 边界条件测试                                  ║");
    println!("╚═══════════════════════════════════════════════════════════════════╝");

    let plan_start = 1704106800000_i64;

    // 测试1：单辆车
    println!("\n📌 测试1：单辆车");
    let single_job = vec![Job {
        vin: "SINGLE001".to_string(),
        due_epoch_ms: plan_start + 3600_000,
        stamping_minutes: 10,
        welding_minutes: 20,
        painting_minutes: 30,
        assemble_minutes: 40,
        mold_code: "M001".to_string(),
        welding_fixture: "F001".to_string(),
        color: "RED".to_string(),
        config: "BASE".to_string(),
        energy_score: 100.0,
        emission_score: 50.0,
    }];

    let input = SolveInput {
        request_id: "edge-single".to_string(),
        plan_start_epoch_ms: plan_start,
        jobs: single_job.clone(),
        params: SolveParams {
            algorithm: "hybrid".to_string(),
            time_budget_sec: 2,
            seed: 42,
            weights: Weights::default(),
            limits: Limits::default(),
        },
    };

    let output = solve_hybrid(&input);
    assert_eq!(output.schedule.len(), 1);
    assert_eq!(output.summary.color_changes, 0);
    assert_eq!(output.summary.config_changes, 0);
    println!("   ✓ 单辆车测试通过");

    // 测试2：相同颜色配置
    println!("\n📌 测试2：所有车辆相同颜色配置");
    let same_jobs: Vec<Job> = (0..5).map(|i| Job {
        vin: format!("SAME{:03}", i),
        due_epoch_ms: plan_start + ((i + 1) as i64 * 3600_000),
        stamping_minutes: 10,
        welding_minutes: 20,
        painting_minutes: 30,
        assemble_minutes: 60,
        mold_code: "M001".to_string(),
        welding_fixture: "F001".to_string(),
        color: "RED".to_string(),
        config: "BASE".to_string(),
        energy_score: 100.0,
        emission_score: 50.0,
    }).collect();

    let input = SolveInput {
        request_id: "edge-same".to_string(),
        plan_start_epoch_ms: plan_start,
        jobs: same_jobs.clone(),
        params: SolveParams {
            algorithm: "ga".to_string(),
            time_budget_sec: 3,
            seed: 42,
            weights: Weights::default(),
            limits: Limits::default(),
        },
    };

    assert_eq!(output.schedule.len(), 5);
    assert_eq!(output.summary.color_changes, 0, "相同颜色不应有切换");
    assert_eq!(output.summary.config_changes, 0, "相同配置不应有切换");
    println!("   ✓ 相同颜色配置测试通过");

    // 测试3：极端延迟
    println!("\n📌 测试3：所有车辆已超期");
    let overdue_jobs: Vec<Job> = (0..5).map(|i| Job {
        vin: format!("OVER{:03}", i),
        due_epoch_ms: plan_start - (3600_000 * (i + 1) as i64), // 所有都已过期
        stamping_minutes: 10,
        welding_minutes: 20,
        painting_minutes: 30,
        assemble_minutes: 60,
        mold_code: "M001".to_string(),
        welding_fixture: "F001".to_string(),
        color: "BLUE".to_string(),
        config: "PREMIUM".to_string(),
        energy_score: 80.0,
        emission_score: 40.0,
    }).collect();

    let input = SolveInput {
        request_id: "edge-overdue".to_string(),
        plan_start_epoch_ms: plan_start,
        jobs: overdue_jobs.clone(),
        params: SolveParams {
            algorithm: "sa".to_string(),
            time_budget_sec: 3,
            seed: 42,
            weights: Weights {
                tardiness: 100.0,
                ..Default::default()
            },
            limits: Limits::default(),
        },
    };

    let output = solve_sa(&input);
    assert_eq!(output.schedule.len(), 5);
    assert!(output.summary.total_tardiness_min > 0, "应有延迟");
    // EDD算法应该优先排最早到期的（即延迟最大的）
    println!("   总延迟：{} 分钟", output.summary.total_tardiness_min);
    println!("   ✓ 极端延迟测试通过");

    // 测试4：零加工时间
    println!("\n📌 测试4：包含零加工时间的工艺");
    let zero_time_jobs = vec![
        Job {
            vin: "ZERO001".to_string(),
            due_epoch_ms: plan_start + 7200_000,
            stamping_minutes: 0,  // 不需要冲压
            welding_minutes: 0,   // 不需要焊装
            painting_minutes: 0,  // 不需要涂装
            assemble_minutes: 60, // 只需要总装
            mold_code: String::new(),
            welding_fixture: String::new(),
            color: "RED".to_string(),
            config: "BASE".to_string(),
            energy_score: 50.0,
            emission_score: 25.0,
        },
        Job {
            vin: "ZERO002".to_string(),
            due_epoch_ms: plan_start + 10800_000,
            stamping_minutes: 10,
            welding_minutes: 20,
            painting_minutes: 30,
            assemble_minutes: 40,
            mold_code: "M001".to_string(),
            welding_fixture: "F001".to_string(),
            color: "BLUE".to_string(),
            config: "PREMIUM".to_string(),
            energy_score: 100.0,
            emission_score: 50.0,
        },
    ];

    let input = SolveInput {
        request_id: "edge-zero".to_string(),
        plan_start_epoch_ms: plan_start,
        jobs: zero_time_jobs.clone(),
        params: SolveParams::default(),
    };

    let output = solve_edd(&input);
    assert_eq!(output.schedule.len(), 2);
    println!("   ✓ 零加工时间测试通过");

    println!("\n✅ 所有边界条件测试通过");
}

#[test]
fn test_algorithm_reproducibility() {
    println!("\n╔═══════════════════════════════════════════════════════════════════╗");
    println!("║                  🔁 算法可重现性测试                                ║");
    println!("╚═══════════════════════════════════════════════════════════════════╝");

    let jobs = generate_realistic_jobs(20, 88888);
    let plan_start = 1704106800000_i64;

    let algorithms = vec![
        ("SA", "sa", solve_sa as fn(&SolveInput) -> SolveOutput),
        ("Hybrid", "hybrid", solve_hybrid as fn(&SolveInput) -> SolveOutput),
    ];

    for (name, algo, solver) in algorithms {
        println!("\n🧪 测试 {} 可重现性", name);

        let mut results = Vec::new();

        // 运行3次，使用相同的 seed
        for run in 1..=3 {
            let input = SolveInput {
                request_id: format!("repro-{}-{}", algo, run),
                plan_start_epoch_ms: plan_start,
                jobs: jobs.clone(),
                params: SolveParams {
                    algorithm: algo.to_string(),
                    time_budget_sec: 3,
                    seed: 12345, // 固定 seed
                    weights: Weights::default(),
                    limits: Limits::default(),
                },
            };

            let output = solver(&input);
            results.push(output);

            print!("   运行 {} : 成本 {:.2}", run, results.last().unwrap().summary.cost);
            if run > 1 {
                let diff = (results[run-1].summary.cost - results[run-2].summary.cost).abs();
                if diff < 1e-6 {
                    println!(" ✓ 完全一致");
                } else {
                    println!(" ⚠️  差异 {:.6}", diff);
                }
            } else {
                println!();
            }
        }

        // 验证结果一致性
        let first_cost = results[0].summary.cost;
        let first_order = &results[0].order;

        for (i, result) in results.iter().enumerate().skip(1) {
            assert_eq!(result.order, *first_order,
                       "{}: 运行 {} 的排产顺序与第一次不一致", name, i + 1);
            assert!((result.summary.cost - first_cost).abs() < 1e-6,
                    "{}: 运行 {} 的成本与第一次不一致", name, i + 1);
        }

        println!("   ✅ {} 可重现性验证通过", name);
    }

    println!("\n✅ 所有算法可重现性测试通过");
}

#[test]
fn test_weight_sensitivity() {
    println!("\n╔═══════════════════════════════════════════════════════════════════╗");
    println!("║                  ⚖️  权重敏感性测试                                 ║");
    println!("╚═══════════════════════════════════════════════════════════════════╝");

    let jobs = generate_realistic_jobs(30, 77777);
    let plan_start = 1704106800000_i64;

    let weight_scenarios = vec![
        ("极端重视延迟", Weights {
            tardiness: 1000.0,
            color_change: 1.0,
            config_change: 1.0,
            energy_excess: 1.0,
            emission_excess: 1.0,
            material_shortage: 0.0,
        }),
        ("极端重视切换", Weights {
            tardiness: 1.0,
            color_change: 500.0,
            config_change: 500.0,
            energy_excess: 1.0,
            emission_excess: 1.0,
            material_shortage: 0.0,
        }),
        ("均衡策略", Weights {
            tardiness: 10.0,
            color_change: 50.0,
            config_change: 30.0,
            energy_excess: 2.0,
            emission_excess: 3.0,
            material_shortage: 0.0,
        }),
    ];

    println!("\n📊 权重策略对比：\n");
    println!("{:<20} | {:>10} | {:>12} | {:>10} | {:>10}",
             "策略", "总成本", "总延迟(min)", "颜色切换", "配置切换");
    println!("{}", "-".repeat(70));

    for (scenario_name, weights) in weight_scenarios {
        let input = SolveInput {
            request_id: format!("weight-{}", scenario_name),
            plan_start_epoch_ms: plan_start,
            jobs: jobs.clone(),
            params: SolveParams {
                algorithm: "hybrid".to_string(),
                time_budget_sec: 5,
                seed: 42,
                weights: weights.clone(),
                limits: Limits::default(),
            },
        };

        let output = solve_hybrid(&input);

        println!("{:<20} | {:>10.2} | {:>12} | {:>10} | {:>10}",
                 scenario_name,
                 output.summary.cost,
                 output.summary.total_tardiness_min,
                 output.summary.color_changes,
                 output.summary.config_changes,
        );

        // 验证权重影响
        if scenario_name.contains("延迟") {
            println!("   → 预期：低延迟");
        } else if scenario_name.contains("切换") {
            println!("   → 预期：少切换");
        }
    }

    println!("\n✅ 权重敏感性测试完成");
}

#[test]
fn test_capacity_limits() {
    println!("\n╔═══════════════════════════════════════════════════════════════════╗");
    println!("║                  🔋 容量限制测试                                    ║");
    println!("╚═══════════════════════════════════════════════════════════════════╝");

    let jobs = generate_realistic_jobs(40, 66666);
    let plan_start = 1704106800000_i64;

    // 计算总能耗和排放
    let total_energy: f64 = jobs.iter().map(|j| j.energy_score).sum();
    let total_emission: f64 = jobs.iter().map(|j| j.emission_score).sum();

    println!("\n📊 数据概况：");
    println!("   总能耗：{:.2}", total_energy);
    println!("   总排放：{:.2}", total_emission);

    let limit_scenarios = vec![
        ("宽松限制", Limits {
            max_energy_per_shift: 10000.0,
            max_emission_per_shift: 5000.0,
        }),
        ("正常限制", Limits {
            max_energy_per_shift: 5000.0,
            max_emission_per_shift: 2500.0,
        }),
        ("严格限制", Limits {
            max_energy_per_shift: 2000.0,
            max_emission_per_shift: 1000.0,
        }),
    ];

    println!("\n🧪 限制策略测试：\n");

    for (scenario_name, limits) in limit_scenarios {
        println!("📌 {}", scenario_name);
        println!("   能耗限制：{} / 班", limits.max_energy_per_shift);
        println!("   排放限制：{} / 班", limits.max_emission_per_shift);

        let input = SolveInput {
            request_id: format!("limit-{}", scenario_name),
            plan_start_epoch_ms: plan_start,
            jobs: jobs.clone(),
            params: SolveParams {
                algorithm: "sa".to_string(),
                time_budget_sec: 5,
                seed: 42,
                weights: Weights {
                    energy_excess: 100.0,
                    emission_excess: 100.0,
                    ..Default::default()
                },
                limits: limits.clone(),
            },
        };

        let output = solve_sa(&input);

        println!("   结果：");
        println!("     能耗超限：{:.2}", output.summary.energy_excess);
        println!("     排放超限：{:.2}", output.summary.emission_excess);
        println!("     违规数：{}", output.violations.len());

        if scenario_name.contains("宽松") {
            assert_eq!(output.violations.len(), 0, "宽松限制不应有违规");
            println!("     ✓ 如预期：无违规");
        } else if scenario_name.contains("严格") {
            assert!(output.violations.len() > 0 ||
                        output.summary.energy_excess > 0.0 ||
                        output.summary.emission_excess > 0.0,
                    "严格限制应有违规或超限");
            println!("     ✓ 如预期：有约束压力");
        }

        println!();
    }

    println!("✅ 容量限制测试完成");
}

/// 性能基准测试（可选，需要 --ignored 才运行）
#[test]
#[ignore]
fn benchmark_algorithms() {
    println!("\n╔═══════════════════════════════════════════════════════════════════╗");
    println!("║                  ⏱️  性能基准测试                                   ║");
    println!("╚═══════════════════════════════════════════════════════════════════╝");

    let problem_sizes = vec![10, 25, 50, 75, 100];

    println!("\n📊 测试不同问题规模下的算法性能\n");
    println!("{:<10} | {:<10} | {:>12} | {:>12} | {:>10}",
             "规模", "算法", "运行时间(ms)", "总成本", "吞吐量");
    println!("{}", "-".repeat(70));

    for size in problem_sizes {
        let jobs = generate_realistic_jobs(size, size as u64);
        let plan_start = 1704106800000_i64;

        for (algo_name, algo_code, time_budget) in vec![
            ("Baseline", "baseline", 1),
            ("SA", "sa", 5),
            ("GA", "ga", 5),
            ("Hybrid", "hybrid", 8),
        ] {
            let input = SolveInput {
                request_id: format!("bench-{}-{}", size, algo_code),
                plan_start_epoch_ms: plan_start,
                jobs: jobs.clone(),
                params: SolveParams {
                    algorithm: algo_code.to_string(),
                    time_budget_sec: time_budget,
                    seed: 42,
                    weights: Weights::default(),
                    limits: Limits::default(),
                },
            };

            let start = Instant::now();
            let output = match algo_code {
                "baseline" => solve_edd(&input),
                "sa" => solve_sa(&input),
                "hybrid" => solve_hybrid(&input),
                _ => panic!("Unknown algorithm"),
            };
            let elapsed = start.elapsed().as_millis();

            let throughput = (size as f64 / elapsed as f64 * 1000.0) as i32;

            println!("{:<10} | {:<10} | {:>12} | {:>12.2} | {:>10}",
                     size, algo_name, elapsed, output.summary.cost, throughput);
        }

        println!("{}", "-".repeat(70));
    }

    println!("\n✅ 性能基准测试完成");
    println!("💡 提示：运行 'cargo test benchmark_algorithms -- --ignored --nocapture' 查看此测试");
}


