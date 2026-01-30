use crate::model::{SolveInput, SolveOutput, Job};
use crate::eval::{build_schedule, build_kpi_summary, build_violations, build_full_process_schedule};
use std::time::Instant;
use std::collections::HashMap;

pub fn solve_edd(input: &SolveInput) -> SolveOutput {
    let start_time = Instant::now();

    let mut sorted_jobs = input.jobs.clone();
    sorted_jobs.sort_by_key(|job| job.due_epoch_ms);

    let schedule = build_schedule(&sorted_jobs, input.plan_start_epoch_ms);

    // 生成详细的工序调度表
    let detailed_schedule = build_full_process_schedule(&sorted_jobs, input.plan_start_epoch_ms);

    let order: Vec<String> = sorted_jobs.iter().map(|job| job.vin.clone()).collect();
    let elapsed_ms = start_time.elapsed().as_millis() as i64;

    // 构建作业映射
    let jobs_by_vin: HashMap<String, Job> = sorted_jobs.iter()
        .map(|job| (job.vin.clone(), job.clone()))
        .collect();

    let summary = build_kpi_summary(
        &schedule,
        &input.params.weights,
        &input.params.limits,
        &jobs_by_vin,
        elapsed_ms
    );

    let violations = build_violations(&schedule, &input.params.limits, &jobs_by_vin);

    let baseline_summary = summary.clone();

    let mut convergence = Vec::new();
    if elapsed_ms > 0 {
        let step = elapsed_ms / 10;
        for i in 0..10 {
            convergence.push(crate::model::ConvergencePoint {
                t_ms: i * step,
                best_cost: summary.cost,
            });
        }
    } else {
        convergence.push(crate::model::ConvergencePoint {
            t_ms: 0,
            best_cost: summary.cost,
        });
    }

    SolveOutput {
        request_id: input.request_id.clone(),
        summary,
        baseline_summary,
        order,
        schedule,
        detailed_schedule,
        violations,
        convergence,
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::model::{Job, SolveParams, Weights, Limits};

    #[test]
    fn test_solve_edd_with_constraints() {
        let input = SolveInput {
            request_id: "test-001".to_string(),
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
                    energy_score: 150.0,
                    emission_score: 75.0,
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
                algorithm: "baseline".to_string(),
                time_budget_sec: 1,
                seed: 42,
                weights: Weights::default(),
                limits: Limits {
                    max_energy_per_shift: 200.0,
                    max_emission_per_shift: 100.0,
                },
            },
        };

        let output = solve_edd(&input);

        // 应该产生超限
        assert!(!output.violations.is_empty());
        assert!(output.summary.energy_excess > 0.0);
        assert!(output.summary.emission_excess > 0.0);
    }
}
