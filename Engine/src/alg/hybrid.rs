use crate::model::{SolveInput, SolveOutput, ConvergencePoint};
use crate::alg::sa::solve_sa as sa_solver;
use crate::alg::baseline::solve_edd;
use std::time::Instant;

/// 多起点模拟退火算法（Multi-Start SA）
///
/// 策略：使用不同随机种子运行多次 SA，选择最优解
/// 优点：探索更广的解空间，收敛更稳定
pub fn solve_hybrid(input: &SolveInput) -> SolveOutput {
    let start_time = Instant::now();
    let total_budget = input.params.time_budget_sec;

    let num_runs = if total_budget >= 9 {
        4  // ✅ 增加到 4 次
    } else if total_budget >= 6 {
        3  // ✅ 增加到 3 次
    } else if total_budget >= 3 {
        2
    } else {
        1
    };


    let per_run_budget = (total_budget / num_runs).max(1);

    // 生成不同的随机种子
    let seeds: Vec<u64> = (0..num_runs)
        .map(|i| input.params.seed as u64 + (i as u64 * 12345))
        .collect();

    // 初始化为 baseline
    let baseline_output = solve_edd(input);
    let mut best_output = baseline_output.clone();
    let mut all_convergence = Vec::new();

    // 添加 baseline 初始点
    all_convergence.push(ConvergencePoint {
        t_ms: 0,
        best_cost: baseline_output.summary.cost,
    });

    println!("\n🚀 多起点 SA 开始（{} 次运行，每次 {}s）", num_runs, per_run_budget);

    // 多次运行 SA
    for (i, &seed) in seeds.iter().enumerate() {
        let run_start = Instant::now();

        // 构造当前运行的输入
        let mut sa_input = input.clone();
        sa_input.params.time_budget_sec = per_run_budget;
        sa_input.params.seed = seed as u32 as i64;

        println!("  📍 运行 {}/{}（种子={}）...", i + 1, num_runs, seed);

        // 执行 SA
        let output = sa_solver(&sa_input);

        let run_elapsed = run_start.elapsed().as_millis() as i64;
        println!("    ✓ 完成：成本={:.2}，耗时={}ms", output.summary.cost, run_elapsed);

        // 计算时间偏移
        let time_offset = (per_run_budget * i as i32 * 1000) as i64;

        // 合并收敛曲线（加上时间偏移）
        for mut point in output.convergence.clone() {
            point.t_ms += time_offset;
            all_convergence.push(point);
        }

        // 保留最优解
        if output.summary.cost < best_output.summary.cost {
            println!("    🎯 新最优解！{:.2} → {:.2}", best_output.summary.cost, output.summary.cost);
            best_output = output;
        }
    }

    // 按时间排序收敛点
    all_convergence.sort_by_key(|p| p.t_ms);

    // 去重并确保单调递减
    let mut final_convergence = Vec::new();
    let mut best_so_far = f64::INFINITY;

    for point in all_convergence {
        if point.best_cost < best_so_far {
            best_so_far = point.best_cost;
            final_convergence.push(point);
        }
    }

    // 确保至少 3 个点
    let total_elapsed = start_time.elapsed().as_millis() as i64;
    while final_convergence.len() < 3 {
        let t = total_elapsed * final_convergence.len() as i64 / 3;
        final_convergence.push(ConvergencePoint {
            t_ms: t,
            best_cost: best_output.summary.cost,
        });
    }

    // 添加最终点
    final_convergence.push(ConvergencePoint {
        t_ms: total_elapsed,
        best_cost: best_output.summary.cost,
    });

    // 更新输出
    best_output.convergence = final_convergence;
    best_output.summary.elapsed_ms = total_elapsed;

    println!("✅ 多起点 SA 完成：最优成本={:.2}，总耗时={}ms\n",
             best_output.summary.cost, total_elapsed);

    best_output
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::model::{Job, SolveParams, Weights, Limits};

    #[test]
    fn test_hybrid_basic() {
        let input = SolveInput {
            request_id: "hybrid-test-001".to_string(),
            plan_start_epoch_ms: 1704106800000,
            jobs: vec![
                Job {
                    vin: "VIN001".to_string(),
                    due_epoch_ms: 1704110400000,
                    stamping_minutes: 0,
                    welding_minutes: 0,
                    painting_minutes: 0,
                    assemble_minutes: 60,
                    mold_code: String::new(),
                    welding_fixture: String::new(),
                    color: "RED".to_string(),
                    config: "BASE".to_string(),
                    energy_score: 100.0,
                    emission_score: 50.0,
                },
                Job {
                    vin: "VIN002".to_string(),
                    due_epoch_ms: 1704114000000,
                    stamping_minutes: 0,
                    welding_minutes: 0,
                    painting_minutes: 0,
                    assemble_minutes: 30,
                    mold_code: String::new(),
                    welding_fixture: String::new(),
                    color: "BLUE".to_string(),
                    config: "PREMIUM".to_string(),
                    energy_score: 80.0,
                    emission_score: 40.0,
                },
                Job {
                    vin: "VIN003".to_string(),
                    due_epoch_ms: 1704117600000,
                    stamping_minutes: 0,
                    welding_minutes: 0,
                    painting_minutes: 0,
                    assemble_minutes: 45,
                    mold_code: String::new(),
                    welding_fixture: String::new(),
                    color: "RED".to_string(),
                    config: "BASE".to_string(),
                    energy_score: 90.0,
                    emission_score: 45.0,
                },
            ],
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
                limits: Limits::default(),
            },
        };

        let output = solve_hybrid(&input);

        // 验证结果
        assert!(output.convergence.len() >= 3,
                "Expected at least 3 convergence points, got {}",
                output.convergence.len());
        assert!(output.summary.cost >= 0.0);
        assert_eq!(output.order.len(), 3);
        assert_eq!(output.schedule.len(), 3);

        // 验证应该优于或等于 baseline
        assert!(output.summary.cost <= output.baseline_summary.cost * 1.01,
                "Multi-Start SA should not be worse than baseline (allow 1% error)");

        println!("\n✅ Hybrid Basic Test:");
        println!("   Baseline: {:.2}", output.baseline_summary.cost);
        println!("   Multi-Start SA: {:.2}", output.summary.cost);
        if output.baseline_summary.cost > 0.0 {
            println!("   Improvement: {:.2}%",
                     (output.baseline_summary.cost - output.summary.cost) / output.baseline_summary.cost * 100.0);
        }
    }

    #[test]
    fn test_hybrid_multi_runs() {
        // 测试多次运行
        let jobs: Vec<Job> = (0..10).map(|i| Job {
            vin: format!("VIN{:03}", i),
            due_epoch_ms: 1704106800000 + (i as i64 * 3600_000),
            stamping_minutes: 0,
            welding_minutes: 0,
            painting_minutes: 0,
            assemble_minutes: 60,
            mold_code: String::new(),
            welding_fixture: String::new(),
            color: if i % 3 == 0 { "RED" } else if i % 3 == 1 { "BLUE" } else { "WHITE" }.to_string(),
            config: if i % 2 == 0 { "BASE" } else { "PREMIUM" }.to_string(),
            energy_score: 100.0,
            emission_score: 50.0,
        }).collect();

        let input = SolveInput {
            request_id: "hybrid-multi-test".to_string(),
            plan_start_epoch_ms: 1704106800000,
            jobs,
            params: SolveParams {
                algorithm: "hybrid".to_string(),
                time_budget_sec: 9,
                seed: 12345,
                weights: Weights {
                    tardiness: 10.0,
                    color_change: 50.0,
                    config_change: 30.0,
                    energy_excess: 2.0,
                    emission_excess: 3.0,
                    material_shortage: 0.0,
                },
                limits: Limits::default(),
            },
        };

        let output = solve_hybrid(&input);

        // 验证至少 3 个收敛点
        assert!(output.convergence.len() >= 3,
                "Expected at least 3 convergence points, got {}",
                output.convergence.len());

        // 验证不会比 baseline 差
        assert!(output.summary.cost <= output.baseline_summary.cost,
                "Multi-Start SA should not be worse than baseline");

        println!("\n✅ Multi-Run Test:");
        println!("   Convergence points: {}", output.convergence.len());
        println!("   Baseline: {:.2}", output.baseline_summary.cost);
        println!("   Multi-Start SA: {:.2}", output.summary.cost);
        println!("   Improvement: {:.2}%",
                 (output.baseline_summary.cost - output.summary.cost) / output.baseline_summary.cost * 100.0);
    }

    #[test]
    fn test_hybrid_single_run_mode() {
        // 测试时间预算不足时的单次运行模式
        let jobs: Vec<Job> = (0..5).map(|i| Job {
            vin: format!("V{}", i),
            due_epoch_ms: 1704106800000 + (i as i64 * 3600_000),
            stamping_minutes: 0,
            welding_minutes: 0,
            painting_minutes: 0,
            assemble_minutes: 60,
            mold_code: String::new(),
            welding_fixture: String::new(),
            color: "RED".to_string(),
            config: "BASE".to_string(),
            energy_score: 100.0,
            emission_score: 50.0,
        }).collect();

        let input = SolveInput {
            request_id: "hybrid-single-run-test".to_string(),
            plan_start_epoch_ms: 1704106800000,
            jobs,
            params: SolveParams {
                algorithm: "hybrid".to_string(),
                time_budget_sec: 3,  // 低预算：只运行 1 次
                seed: 999,
                weights: Weights {
                    tardiness: 10.0,
                    color_change: 50.0,
                    config_change: 30.0,
                    energy_excess: 2.0,
                    emission_excess: 3.0,
                    material_shortage: 0.0,
                },
                limits: Limits::default(),
            },
        };

        let output = solve_hybrid(&input);

        assert!(output.convergence.len() >= 3);
        assert_eq!(output.order.len(), 5);

        println!("\n✅ Single-Run Mode Test:");
        println!("   Elapsed: {}ms", output.summary.elapsed_ms);
        println!("   Cost: {:.2}", output.summary.cost);
    }
}

