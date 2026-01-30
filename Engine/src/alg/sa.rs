use crate::model::{SolveInput, SolveOutput, Job, ConvergencePoint};
use crate::eval::{build_schedule, build_full_process_schedule, build_kpi_summary, build_violations};  // ✅ 添加 build_full_process_schedule
use std::time::Instant;
use std::collections::HashMap;
use rand::prelude::*;

/// 模拟退火算法 - 优化版
pub fn solve_sa(input: &SolveInput) -> SolveOutput {
    let start_time = Instant::now();

    // 初始化随机数生成器
    let mut rng = StdRng::seed_from_u64(input.params.seed as u64);

    // 获取时间预算（毫秒）
    let time_budget_ms = (input.params.time_budget_sec as u64 * 1000).max(1000);

    // 初始解选择策略
    let mut current_jobs = if !input.params.algorithm.starts_with("hybrid_") {
        // ✅ 改进1：优先使用颜色分组排序（而非 EDD）
        let mut jobs = input.jobs.clone();
        jobs.sort_by(|a, b| {
            a.color.cmp(&b.color)
                .then(a.config.cmp(&b.config))
                .then(a.due_epoch_ms.cmp(&b.due_epoch_ms))
        });
        jobs
    } else {
        input.jobs.clone()
    };

    // 构建作业映射
    let jobs_by_vin: HashMap<String, Job> = input.jobs.iter()
        .map(|job| (job.vin.clone(), job.clone()))
        .collect();

    // 计算初始解
    let mut current_schedule = build_schedule(&current_jobs, input.plan_start_epoch_ms);
    let mut current_summary = build_kpi_summary(
        &current_schedule,
        &input.params.weights,
        &input.params.limits,
        &jobs_by_vin,
        0
    );

    // 最优解记录
    let mut best_jobs = current_jobs.clone();
    let mut best_summary = current_summary.clone();
    let mut best_schedule = current_schedule.clone();

    // ✅ 改进2：自适应温度计划
    let initial_temp = best_summary.cost.max(1000.0);
    let final_temp = 0.1; // 降低最终温度，提高精度

    let total_iterations_estimate = (time_budget_ms / 3) as f64; // 更保守的估计
    let cooling_rate = (final_temp / initial_temp).powf(1.0 / total_iterations_estimate);

    let mut temperature = initial_temp;
    let mut _iteration = 0;
    let mut accepted_moves = 0;
    let mut rejected_moves = 0;

    // 收敛记录
    let mut convergence = Vec::new();
    convergence.push(ConvergencePoint {
        t_ms: 0,
        best_cost: best_summary.cost,
    });

    let mut last_record_time = 0;
    let mut no_improvement_count = 0;

    loop {
        let elapsed = start_time.elapsed().as_millis() as u64;

        // 检查时间预算
        if elapsed >= time_budget_ms {
            break;
        }

        // ✅ 改进3：多样化的邻域操作
        let mut new_jobs = current_jobs.clone();
        let n = new_jobs.len();

        if n > 1 {
            let operation = rng.gen_range(0..100);

            if operation < 70 {
                // 70% 概率：Swap（交换两个位置）
                let i = rng.gen_range(0..n);
                let j = rng.gen_range(0..n);
                new_jobs.swap(i, j);
            } else if operation < 90 {
                // 20% 概率：Reverse（反转一段）
                let len = rng.gen_range(2..=(n/4).max(2));
                let start = rng.gen_range(0..=(n - len));
                new_jobs[start..(start + len)].reverse();
            } else {
                // 10% 概率：Insert（移动一个元素）
                let from = rng.gen_range(0..n);
                let to = rng.gen_range(0..n);
                if from != to {
                    let job = new_jobs.remove(from);
                    new_jobs.insert(to, job);
                }
            }
        }

        // 计算新解
        let new_schedule = build_schedule(&new_jobs, input.plan_start_epoch_ms);
        let new_summary = build_kpi_summary(
            &new_schedule,
            &input.params.weights,
            &input.params.limits,
            &jobs_by_vin,
            elapsed as i64
        );

        // 计算成本差
        let delta = new_summary.cost - current_summary.cost;

        // ✅ 改进4：改进的接受准则
        let accept = if delta < 0.0 {
            true
        } else {
            let ratio = -delta / temperature.max(1e-6);
            let prob = if ratio < -50.0 {
                0.0
            } else {
                ratio.exp()
            };
            rng.gen_bool(prob)
        };

        if accept {
            accepted_moves += 1;
            current_jobs = new_jobs;
            current_schedule = new_schedule;
            current_summary = new_summary;

            // 更新最优解
            if current_summary.cost < best_summary.cost {
                best_jobs = current_jobs.clone();
                best_summary = current_summary.clone();
                best_schedule = current_schedule.clone();
                no_improvement_count = 0;
            } else {
                no_improvement_count += 1;
            }
        } else {
            rejected_moves += 1;
            no_improvement_count += 1;
        }

        // ✅ 改进5：自适应降温（考虑接受率）
        if (accepted_moves + rejected_moves) % 100 == 0 {
            let acceptance_rate = accepted_moves as f64 / (accepted_moves + rejected_moves) as f64;

            // 如果接受率过低，加速降温；如果过高，减缓降温
            if acceptance_rate < 0.02 {
                temperature *= 0.95; // 快速降温
            } else if acceptance_rate > 0.3 {
                temperature *= 0.99; // 慢速降温
            } else {
                temperature *= cooling_rate; // 正常降温
            }

            accepted_moves = 0;
            rejected_moves = 0;
        } else {
            temperature *= cooling_rate;
        }

        temperature = temperature.max(final_temp);
        _iteration += 1;

        // ✅ 改进6：提前终止（1000次迭代无改进）
        if no_improvement_count > 1000 {
            break;
        }

        // 每200ms记录一次收敛点
        if elapsed - last_record_time >= 200 {
            convergence.push(ConvergencePoint {
                t_ms: elapsed as i64,
                best_cost: best_summary.cost,
            });
            last_record_time = elapsed;
        }
    }

    // 确保至少有一个收敛点
    if convergence.len() < 3 {
        let elapsed = start_time.elapsed().as_millis() as i64;
        convergence.push(ConvergencePoint {
            t_ms: elapsed,
            best_cost: best_summary.cost,
        });
    }

    let elapsed_ms = start_time.elapsed().as_millis() as i64;
    let order = best_jobs.iter().map(|job| job.vin.clone()).collect();

    // ✅ 新增：生成详细工序调度表
    let detailed_schedule = build_full_process_schedule(&best_jobs, input.plan_start_epoch_ms);

    let violations = build_violations(&best_schedule, &input.params.limits, &jobs_by_vin);

    let final_summary = build_kpi_summary(
        &best_schedule,
        &input.params.weights,
        &input.params.limits,
        &jobs_by_vin,
        elapsed_ms
    );

    SolveOutput {
        request_id: input.request_id.clone(),
        summary: final_summary,
        baseline_summary: {
            let mut edd_jobs = input.jobs.clone();
            edd_jobs.sort_by_key(|job| job.due_epoch_ms);
            let edd_schedule = build_schedule(&edd_jobs, input.plan_start_epoch_ms);
            build_kpi_summary(
                &edd_schedule,
                &input.params.weights,
                &input.params.limits,
                &jobs_by_vin,
                0,
            )
        },
        order,
        schedule: best_schedule,
        detailed_schedule,  // ✅ 新增
        violations,
        convergence,
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::model::{Job, SolveParams, Weights, Limits};

    #[test]
    fn test_sa_improvement() {
        let input = SolveInput {
            request_id: "sa-test-001".to_string(),
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
                algorithm: "sa".to_string(),
                time_budget_sec: 5,
                seed: 42,
                weights: Weights::default(),
                limits: Limits::default(),
            },
        };

        let output = solve_sa(&input);

        // 验证有收敛记录
        assert!(output.convergence.len() >= 1);

        // 验证结果
        assert!(output.summary.cost >= 0.0);
        assert!(output.baseline_summary.cost >= output.summary.cost);
        // 验证收敛点数 >= 1
        assert!(output.convergence.len() >= 1);
    }

    #[test]
    fn test_sa_reproducible() {
        let input = SolveInput {
            request_id: "sa-repro-001".to_string(),
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
            ],
            params: SolveParams {
                algorithm: "sa".to_string(),
                time_budget_sec: 1,
                seed: 12345,
                weights: Weights::default(),
                limits: Limits::default(),
            },
        };

        let output1 = solve_sa(&input);
        let output2 = solve_sa(&input);

        // 相同seed应产生相同结果
        assert_eq!(output1.order, output2.order);
    }
}


