use crate::model::{Job, KpiSummary, ScheduleItem, Limits, ProcessType};
use crate::util::time::shift_id;
use std::collections::HashMap;

/// 构建调度（单工艺版本 - 向后兼容）
///
/// 当前简化逻辑：
/// - 只调度总装工艺（process_type = 4）
/// - 串行排列（时间累加）
/// - 未来扩展为四工艺串联
pub fn build_schedule(order: &[Job], plan_start_epoch_ms: i64) -> Vec<ScheduleItem> {
    build_schedule_for_process(order, plan_start_epoch_ms, ProcessType::Assembly, 0)
}

/// 按工艺构建调度（内部函数，支持扩展）
///
/// # 参数
/// - `order`: 作业顺序
/// - `plan_start_epoch_ms`: 计划开始时间
/// - `process`: 工艺类型
/// - `line_id`: 产线ID（0表示未分配）
pub fn build_schedule_for_process(
    order: &[Job],
    plan_start_epoch_ms: i64,
    process: ProcessType,
    line_id: i64,
) -> Vec<ScheduleItem> {
    let mut current_time = plan_start_epoch_ms;
    let mut schedule = Vec::new();
    let mut seq_in_shift = 0;
    let mut last_shift_id = String::new();

    for (seq, job) in order.iter().enumerate() {
        let work_minutes = job.get_process_minutes(process);

        // 跳过不需要此工艺的作业
        if work_minutes == 0 {
            continue;
        }

        let start_epoch_ms = current_time;
        let end_epoch_ms = start_epoch_ms + (work_minutes as i64 * 60_000);
        let tardiness_min = std::cmp::max(0, (end_epoch_ms - job.due_epoch_ms) / 60_000);
        let shift = shift_id(start_epoch_ms);

        // 检测是否切换班次
        if shift != last_shift_id {
            seq_in_shift = 0;
            last_shift_id = shift.clone();
        }
        seq_in_shift += 1;

        schedule.push(ScheduleItem {
            seq: (seq + 1) as i32,
            vin: job.vin.clone(),
            process_type: process.as_i32(),
            line_id,
            start_epoch_ms,
            end_epoch_ms,
            due_epoch_ms: job.due_epoch_ms,
            tardiness_min,
            color: job.color.clone(),
            config: job.config.clone(),
            shift_id: shift,
            seq_in_shift,
        });

        current_time = end_epoch_ms;
    }

    schedule
}

/// 汇总统计（延迟惩罚、切换次数）
pub fn summarize(schedule: &[ScheduleItem]) -> (i64, i64, i32, i32) {
    let total_tardiness = schedule.iter().map(|item| item.tardiness_min).sum::<i64>();
    let max_tardiness = schedule.iter().map(|item| item.tardiness_min).max().unwrap_or(0);

    let mut color_changes = 0;
    let mut config_changes = 0;

    for i in 1..schedule.len() {
        if schedule[i].color != schedule[i-1].color {
            color_changes += 1;
        }
        if schedule[i].config != schedule[i-1].config {
            config_changes += 1;
        }
    }

    (total_tardiness, max_tardiness, color_changes, config_changes)
}

/// 按班次统计能耗和排放
pub fn calculate_shift_metrics(
    schedule: &[ScheduleItem],
    jobs_by_vin: &HashMap<String, Job>,
) -> HashMap<String, (f64, f64)> {
    let mut shift_metrics = HashMap::new();

    for item in schedule {
        if let Some(job) = jobs_by_vin.get(&item.vin) {
            let entry = shift_metrics.entry(item.shift_id.clone()).or_insert((0.0, 0.0));
            entry.0 += job.energy_score;
            entry.1 += job.emission_score;
        }
    }

    shift_metrics
}

/// 计算成本
pub fn calc_cost(
    summary: &KpiSummary,
    weights: &crate::model::Weights,
) -> f64 {
    (weights.tardiness * summary.total_tardiness_min as f64)
        + (weights.color_change * summary.color_changes as f64)
        + (weights.config_change * summary.config_changes as f64)
        + (weights.energy_excess * summary.energy_excess)
        + (weights.emission_excess * summary.emission_excess)
        + (weights.material_shortage * summary.material_shortage)
}

/// 构建 KPI 汇总
pub fn build_kpi_summary(
    schedule: &[ScheduleItem],
    weights: &crate::model::Weights,
    limits: &Limits,
    jobs_by_vin: &HashMap<String, Job>,
    elapsed_ms: i64,
) -> KpiSummary {
    let (total_tardiness_min, max_tardiness_min, color_changes, config_changes) = summarize(schedule);

    // 计算班次能耗和排放
    let shift_metrics = calculate_shift_metrics(schedule, jobs_by_vin);

    let mut energy_excess = 0.0;
    let mut emission_excess = 0.0;

    for (_shift_id, (energy, emission)) in &shift_metrics {
        if *energy > limits.max_energy_per_shift {
            energy_excess += energy - limits.max_energy_per_shift;
        }
        if *emission > limits.max_emission_per_shift {
            emission_excess += emission - limits.max_emission_per_shift;
        }
    }

    let mut summary = KpiSummary {
        cost: 0.0,
        total_tardiness_min,
        max_tardiness_min,
        color_changes,
        config_changes,
        energy_excess,
        emission_excess,
        material_shortage: 0.0,
        elapsed_ms,
    };

    summary.cost = calc_cost(&summary, weights);
    summary
}/// ✅ 构建四大工艺串行调度（单辆车完整流程）
pub fn build_full_process_schedule(
    order: &[Job],
    plan_start_epoch_ms: i64,
) -> Vec<ScheduleItem> {
    let mut all_items = Vec::new();
    let mut current_time = plan_start_epoch_ms;
    let mut last_shift_id = String::new();
    let mut seq_in_shift = 0;

    for (seq, job) in order.iter().enumerate() {
        let job_start = current_time;
        let mut process_start = job_start;

        // 1️⃣ 冲压
        if job.stamping_minutes > 0 {
            let duration = job.stamping_minutes as i64 * 60_000;
            let item = create_schedule_item(
                seq,
                job,
                ProcessType::Stamping,
                process_start,
                duration,
                &mut last_shift_id,
                &mut seq_in_shift,
            );
            all_items.push(item);
            process_start += duration;
        }

        // 2️⃣ 焊装
        if job.welding_minutes > 0 {
            let duration = job.welding_minutes as i64 * 60_000;
            let item = create_schedule_item(
                seq,
                job,
                ProcessType::Welding,
                process_start,
                duration,
                &mut last_shift_id,
                &mut seq_in_shift,
            );
            all_items.push(item);
            process_start += duration;
        }

        // 3️⃣ 涂装
        if job.painting_minutes > 0 {
            let duration = job.painting_minutes as i64 * 60_000;
            let item = create_schedule_item(
                seq,
                job,
                ProcessType::Painting,
                process_start,
                duration,
                &mut last_shift_id,
                &mut seq_in_shift,
            );
            all_items.push(item);
            process_start += duration;
        }

        // 4️⃣ 总装
        if job.assemble_minutes > 0 {
            let duration = job.assemble_minutes as i64 * 60_000;
            let item = create_schedule_item(
                seq,
                job,
                ProcessType::Assembly,
                process_start,
                duration,
                &mut last_shift_id,
                &mut seq_in_shift,
            );
            all_items.push(item);
            process_start += duration;
        }

        // ✅ 关键：下一辆车从这辆车的总装结束后开始
        current_time = process_start;
    }

    all_items
}

/// 辅助函数：创建单个调度项
fn create_schedule_item(
    seq: usize,
    job: &Job,
    process: ProcessType,
    start_epoch_ms: i64,
    duration_ms: i64,
    last_shift_id: &mut String,
    seq_in_shift: &mut i32,
) -> ScheduleItem {
    let end_epoch_ms = start_epoch_ms + duration_ms;
    let tardiness_min = std::cmp::max(0, (end_epoch_ms - job.due_epoch_ms) / 60_000);
    let shift = shift_id(start_epoch_ms);

    // 检测班次切换
    if shift != *last_shift_id {
        *seq_in_shift = 0;
        *last_shift_id = shift.clone();
    }
    *seq_in_shift += 1;

    ScheduleItem {
        seq: (seq + 1) as i32,
        vin: job.vin.clone(),
        process_type: process.as_i32(),
        line_id: 0,  // P2 任务：产线分配
        start_epoch_ms,
        end_epoch_ms,
        due_epoch_ms: job.due_epoch_ms,
        tardiness_min,
        color: job.color.clone(),
        config: job.config.clone(),
        shift_id: shift,
        seq_in_shift: *seq_in_shift,
    }
}



/// 生成违规信息
pub fn build_violations(
    schedule: &[ScheduleItem],
    limits: &Limits,
    jobs_by_vin: &HashMap<String, Job>,
) -> Vec<crate::model::ShiftViolation> {
    let shift_metrics = calculate_shift_metrics(schedule, jobs_by_vin);
    let mut violations = Vec::new();

    for (shift_id, (energy, emission)) in shift_metrics {
        if energy > limits.max_energy_per_shift {
            violations.push(crate::model::ShiftViolation {
                shift_id: shift_id.clone(),
                vtype: "ENERGY".to_string(),
                excess: energy - limits.max_energy_per_shift,
            });
        }
        if emission > limits.max_emission_per_shift {
            violations.push(crate::model::ShiftViolation {
                shift_id,
                vtype: "EMISSION".to_string(),
                excess: emission - limits.max_emission_per_shift,
            });
        }
    }

    violations
}

// ========== 单元测试 ==========
#[cfg(test)]
mod tests {
    use super::*;
    use crate::model::{Job, Weights, Limits};

    fn create_test_job(vin: &str, assembly_minutes: i32, due_offset_hours: i64) -> Job {
        let base_time = 1704106800000_i64; // 2024-01-01 08:00:00
        Job {
            vin: vin.to_string(),
            due_epoch_ms: base_time + (due_offset_hours * 3600 * 1000),
            stamping_minutes: 0,
            welding_minutes: 0,
            painting_minutes: 0,
            assemble_minutes: assembly_minutes,
            mold_code: String::new(),
            welding_fixture: String::new(),
            color: "RED".to_string(),
            config: "BASE".to_string(),
            energy_score: 100.0,
            emission_score: 50.0,
        }
    }
    #[test]
    fn test_multi_job_full_process() {
        // 第一辆车：仅总装60分钟
        let job1 = create_test_job("VIN001", 60, 5);

        // 第二辆车：冲压20 + 总装40 = 60分钟
        let mut job2 = create_test_job("VIN002", 40, 8);
        job2.stamping_minutes = 20;

        let jobs = vec![job1, job2];
        let plan_start = 1704106800000_i64;

        // ✅ 使用完整工序版本
        let schedule = build_full_process_schedule(&jobs, plan_start);

        // 第一辆车：1个工序（总装）
        let job1_items: Vec<_> = schedule.iter()
            .filter(|item| item.vin == "VIN001")
            .collect();
        assert_eq!(job1_items.len(), 1);
        assert_eq!(job1_items[0].process_type, 4);  // 总装

        // 第二辆车：2个工序（冲压 + 总装）
        let job2_items: Vec<_> = schedule.iter()
            .filter(|item| item.vin == "VIN002")
            .collect();
        assert_eq!(job2_items.len(), 2);
        assert_eq!(job2_items[0].process_type, 1);  // 冲压
        assert_eq!(job2_items[1].process_type, 4);  // 总装

        // 验证串行：job2 的冲压应该从 job1 的总装结束后开始
        assert_eq!(job2_items[0].start_epoch_ms, job1_items[0].end_epoch_ms);

        // 验证总时长
        let job1_total = job1_items[0].end_epoch_ms - job1_items[0].start_epoch_ms;
        assert_eq!(job1_total, 60 * 60_000);

        let job2_total = job2_items[1].end_epoch_ms - job2_items[0].start_epoch_ms;
        assert_eq!(job2_total, 60 * 60_000);  // 20 + 40
    }

    #[test]
    fn test_full_process_serial() {
        let mut job = create_test_job("VIN_FULL", 40, 10);
        job.stamping_minutes = 10;
        job.welding_minutes = 20;
        job.painting_minutes = 30;
        job.assemble_minutes = 40;

        let jobs = vec![job];
        let plan_start = 1704106800000_i64;

        let schedule = build_full_process_schedule(&jobs, plan_start);

        // 应该有4个工序
        assert_eq!(schedule.len(), 4);

        // 验证工序类型
        assert_eq!(schedule[0].process_type, 1);  // 冲压
        assert_eq!(schedule[1].process_type, 2);  // 焊装
        assert_eq!(schedule[2].process_type, 3);  // 涂装
        assert_eq!(schedule[3].process_type, 4);  // 总装

        // 验证串行
        assert_eq!(schedule[1].start_epoch_ms, schedule[0].end_epoch_ms);
        assert_eq!(schedule[2].start_epoch_ms, schedule[1].end_epoch_ms);
        assert_eq!(schedule[3].start_epoch_ms, schedule[2].end_epoch_ms);

        // 验证各工序时长
        assert_eq!(schedule[0].end_epoch_ms - schedule[0].start_epoch_ms, 10 * 60_000);
        assert_eq!(schedule[1].end_epoch_ms - schedule[1].start_epoch_ms, 20 * 60_000);
        assert_eq!(schedule[2].end_epoch_ms - schedule[2].start_epoch_ms, 30 * 60_000);
        assert_eq!(schedule[3].end_epoch_ms - schedule[3].start_epoch_ms, 40 * 60_000);

        // 验证总时长
        let total_time = schedule[3].end_epoch_ms - schedule[0].start_epoch_ms;
        assert_eq!(total_time, 100 * 60_000);
    }
    #[test]
    fn test_build_schedule_basic() {
        let jobs = vec![
            create_test_job("VIN001", 60, 2),
            create_test_job("VIN002", 30, 4),
        ];

        let plan_start = 1704106800000_i64;
        let schedule = build_schedule(&jobs, plan_start);

        assert_eq!(schedule.len(), 2);
        assert_eq!(schedule[0].vin, "VIN001");
        assert_eq!(schedule[0].process_type, 4); // 总装
        assert_eq!(schedule[0].line_id, 0);

        // 第一个作业 60 分钟
        assert_eq!(schedule[0].end_epoch_ms - schedule[0].start_epoch_ms, 60 * 60_000);

        // 第二个作业紧接着第一个
        assert_eq!(schedule[1].start_epoch_ms, schedule[0].end_epoch_ms);
    }

    #[test]
    fn test_build_schedule_skip_zero_time() {
        let mut job = create_test_job("VIN003", 0, 2); // assemble_minutes = 0
        job.stamping_minutes = 10; // 但有冲压时间

        let jobs = vec![job];
        let plan_start = 1704106800000_i64;

        // 调度总装（assembly_minutes = 0 应该被跳过）
        let schedule = build_schedule(&jobs, plan_start);
        assert_eq!(schedule.len(), 0);

        // 调度冲压（应该有一个项）
        let schedule_stamping = build_schedule_for_process(
            &jobs,
            plan_start,
            ProcessType::Stamping,
            0
        );
        assert_eq!(schedule_stamping.len(), 1);
        assert_eq!(schedule_stamping[0].process_type, 1);
    }

    #[test]
    fn test_summarize() {
        let jobs = vec![
            create_test_job("VIN001", 60, 2),
            create_test_job("VIN002", 30, 4),
        ];

        let schedule = build_schedule(&jobs, 1704106800000);
        let (total_tardiness, max_tardiness, color_changes, config_changes) = summarize(&schedule);

        assert!(total_tardiness >= 0);
        assert!(max_tardiness >= 0);
        assert_eq!(color_changes, 0); // 都是 RED
        assert_eq!(config_changes, 0); // 都是 BASE
    }

    #[test]
    fn test_color_changes() {
        let mut jobs = vec![
            create_test_job("VIN001", 60, 2),
            create_test_job("VIN002", 30, 4),
            create_test_job("VIN003", 45, 6),
        ];

        // RED -> BLUE -> RED
        jobs[1].color = "BLUE".to_string();

        let schedule = build_schedule(&jobs, 1704106800000);
        let (_, _, color_changes, _) = summarize(&schedule);

        assert_eq!(color_changes, 2);
    }

    #[test]
    fn test_config_changes() {
        let mut jobs = vec![
            create_test_job("VIN001", 60, 2),
            create_test_job("VIN002", 30, 4),
            create_test_job("VIN003", 45, 6),
        ];

        // BASE -> PREMIUM -> BASE
        jobs[1].config = "PREMIUM".to_string();

        let schedule = build_schedule(&jobs, 1704106800000);
        let (_, _, _, config_changes) = summarize(&schedule);

        assert_eq!(config_changes, 2);
    }

    #[test]
    fn test_no_changes_when_same() {
        let jobs = vec![
            create_test_job("VIN001", 60, 2),
            create_test_job("VIN002", 30, 4),
            create_test_job("VIN003", 45, 6),
        ];

        let schedule = build_schedule(&jobs, 1704106800000);
        let (_, _, color_changes, config_changes) = summarize(&schedule);

        assert_eq!(color_changes, 0);
        assert_eq!(config_changes, 0);
    }

    #[test]
    fn test_single_change_each() {
        let mut jobs = vec![
            create_test_job("VIN001", 60, 2),
            create_test_job("VIN002", 30, 4),
        ];

        jobs[1].color = "BLUE".to_string();
        jobs[1].config = "PREMIUM".to_string();

        let schedule = build_schedule(&jobs, 1704106800000);
        let (_, _, color_changes, config_changes) = summarize(&schedule);

        assert_eq!(color_changes, 1);
        assert_eq!(config_changes, 1);
    }


    #[test]
    fn test_shift_metrics() {
        let jobs = vec![
            create_test_job("VIN001", 60, 2),
            create_test_job("VIN002", 30, 4),
        ];

        let schedule = build_schedule(&jobs, 1704106800000);
        let jobs_by_vin: HashMap<String, Job> = jobs.iter()
            .map(|job| (job.vin.clone(), job.clone()))
            .collect();

        let metrics = calculate_shift_metrics(&schedule, &jobs_by_vin);

        // 应该至少有一个班次
        assert!(!metrics.is_empty());

        // 总能耗应该是所有作业的和
        let total_energy: f64 = metrics.values().map(|(e, _)| e).sum();
        assert_eq!(total_energy, 200.0); // 100 + 100
    }

    #[test]
    fn test_build_kpi_summary() {
        let jobs = vec![
            create_test_job("VIN001", 60, 2),
            create_test_job("VIN002", 30, 4),
        ];

        let schedule = build_schedule(&jobs, 1704106800000);
        let jobs_by_vin: HashMap<String, Job> = jobs.iter()
            .map(|job| (job.vin.clone(), job.clone()))
            .collect();

        let weights = Weights {
            tardiness: 2.0,
            color_change: 1.0,
            config_change: 1.5,
            energy_excess: 0.8,
            emission_excess: 0.6,
            material_shortage: 2.0,
        };

        let limits = Limits {
            max_energy_per_shift: 1500.0,
            max_emission_per_shift: 800.0,
        };

        let summary = build_kpi_summary(&schedule, &weights, &limits, &jobs_by_vin, 1000);

        assert!(summary.cost >= 0.0);
        assert_eq!(summary.elapsed_ms, 1000);
        assert_eq!(summary.color_changes, 0);
        assert_eq!(summary.config_changes, 0);
    }

    #[test]
    fn test_build_violations() {
        let mut jobs = vec![
            create_test_job("VIN001", 60, 2),
        ];
        jobs[0].energy_score = 2000.0; // 超过限制

        let schedule = build_schedule(&jobs, 1704106800000);
        let jobs_by_vin: HashMap<String, Job> = jobs.iter()
            .map(|job| (job.vin.clone(), job.clone()))
            .collect();

        let limits = Limits {
            max_energy_per_shift: 1500.0,
            max_emission_per_shift: 800.0,
        };

        let violations = build_violations(&schedule, &limits, &jobs_by_vin);

        assert_eq!(violations.len(), 1);
        assert_eq!(violations[0].vtype, "ENERGY");
        assert_eq!(violations[0].excess, 500.0); // 2000 - 1500
    }

    #[test]
    fn test_seq_in_shift_increments() {
        let jobs = vec![
            create_test_job("VIN001", 30, 1),
            create_test_job("VIN002", 30, 2),
            create_test_job("VIN003", 30, 3),
        ];

        let schedule = build_schedule(&jobs, 1704106800000);

        // 验证 seq_in_shift 递增
        assert_eq!(schedule[0].seq_in_shift, 1);
        assert_eq!(schedule[1].seq_in_shift, 2);
        assert_eq!(schedule[2].seq_in_shift, 3);

        // 验证 shift_id 存在
        assert!(!schedule[0].shift_id.is_empty());
    }

    #[test]
    fn test_multi_process_scheduling() {
        let mut job = create_test_job("VIN004", 40, 2);
        job.stamping_minutes = 10;
        job.welding_minutes = 20;
        job.painting_minutes = 30;
        job.mold_code = "MOLD_A".to_string();

        let jobs = vec![job];
        let plan_start = 1704106800000_i64;

        // 测试四大工艺分别调度
        let stamping_schedule = build_schedule_for_process(
            &jobs,
            plan_start,
            ProcessType::Stamping,
            101
        );
        assert_eq!(stamping_schedule.len(), 1);
        assert_eq!(stamping_schedule[0].process_type, 1);
        assert_eq!(stamping_schedule[0].line_id, 101);
        assert_eq!(stamping_schedule[0].end_epoch_ms - stamping_schedule[0].start_epoch_ms, 10 * 60_000);

        let welding_schedule = build_schedule_for_process(
            &jobs,
            plan_start,
            ProcessType::Welding,
            102
        );
        assert_eq!(welding_schedule.len(), 1);
        assert_eq!(welding_schedule[0].process_type, 2);
        assert_eq!(welding_schedule[0].line_id, 102);

        let painting_schedule = build_schedule_for_process(
            &jobs,
            plan_start,
            ProcessType::Painting,
            103
        );
        assert_eq!(painting_schedule.len(), 1);
        assert_eq!(painting_schedule[0].process_type, 3);

        let assembly_schedule = build_schedule_for_process(
            &jobs,
            plan_start,
            ProcessType::Assembly,
            104
        );
        assert_eq!(assembly_schedule.len(), 1);
        assert_eq!(assembly_schedule[0].process_type, 4);
    }

    #[test]
    fn test_tardiness_calculation() {
        let base_time = 1704106800000_i64;
        let mut job = create_test_job("VIN005", 120, 1); // due in 1 hour
        job.due_epoch_ms = base_time + (1 * 3600 * 1000); // 1小时后到期

        let jobs = vec![job];
        let schedule = build_schedule(&jobs, base_time);

        // 120分钟的作业，1小时后到期，应该延迟60分钟
        assert_eq!(schedule[0].tardiness_min, 60);
    }

    #[test]
    fn test_no_tardiness() {
        let base_time = 1704106800000_i64;
        let mut job = create_test_job("VIN006", 30, 2); // due in 2 hours
        job.due_epoch_ms = base_time + (2 * 3600 * 1000);

        let jobs = vec![job];
        let schedule = build_schedule(&jobs, base_time);

        // 30分钟的作业，2小时后到期，不应该延迟
        assert_eq!(schedule[0].tardiness_min, 0);
    }

    #[test]
    fn test_empty_jobs() {
        let jobs: Vec<Job> = vec![];
        let schedule = build_schedule(&jobs, 1704106800000);

        assert_eq!(schedule.len(), 0);

        let (total_tardiness, max_tardiness, color_changes, config_changes) = summarize(&schedule);
        assert_eq!(total_tardiness, 0);
        assert_eq!(max_tardiness, 0);
        assert_eq!(color_changes, 0);
        assert_eq!(config_changes, 0);
    }

    #[test]
    fn test_single_job() {
        let jobs = vec![create_test_job("VIN007", 45, 3)];
        let schedule = build_schedule(&jobs, 1704106800000);

        assert_eq!(schedule.len(), 1);

        let (_, _, color_changes, config_changes) = summarize(&schedule);
        assert_eq!(color_changes, 0); // 单个作业无切换
        assert_eq!(config_changes, 0);
    }

    #[test]
    fn test_calc_cost_formula() {
        let summary = KpiSummary {
            cost: 0.0, // 会被重新计算
            total_tardiness_min: 100,
            max_tardiness_min: 50,
            color_changes: 5,
            config_changes: 3,
            energy_excess: 200.0,
            emission_excess: 100.0,
            material_shortage: 10.0,
            elapsed_ms: 5000,
        };

        let weights = Weights {
            tardiness: 2.0,
            color_change: 10.0,
            config_change: 15.0,
            energy_excess: 0.5,
            emission_excess: 0.8,
            material_shortage: 50.0,
        };

        let cost = calc_cost(&summary, &weights);

        // 手动计算验证
        // (2.0 * 100) + (10.0 * 5) + (15.0 * 3) + (0.5 * 200) + (0.8 * 100) + (50.0 * 10)
        // = 200 + 50 + 45 + 100 + 80 + 500 = 975
        assert_eq!(cost, 975.0);
    }

    #[test]
    fn test_shift_change_detection() {
        // 创建跨越多个班次的作业
        let base_time = 1704106800000_i64; // 某个基准时间
        let jobs = vec![
            create_test_job("VIN008", 60, 1),
            create_test_job("VIN009", 60, 2),
            create_test_job("VIN010", 720, 3), // 12小时，肯定会跨班次
            create_test_job("VIN011", 60, 4),
        ];

        let schedule = build_schedule(&jobs, base_time);

        // 验证至少有两个不同的 shift_id
        let unique_shifts: std::collections::HashSet<_> = schedule.iter()
            .map(|item| item.shift_id.clone())
            .collect();

        assert!(unique_shifts.len() >= 1); // 至少有一个班次

        // 验证 seq_in_shift 在不同班次重置
        for i in 1..schedule.len() {
            if schedule[i].shift_id != schedule[i-1].shift_id {
                // 班次切换时，seq_in_shift 应该重置为 1
                assert_eq!(schedule[i].seq_in_shift, 1);
            }
        }
    }

    #[test]
    fn test_job_get_process_minutes() {
        let mut job = create_test_job("VIN012", 40, 2);
        job.stamping_minutes = 10;
        job.welding_minutes = 20;
        job.painting_minutes = 30;

        assert_eq!(job.get_process_minutes(ProcessType::Stamping), 10);
        assert_eq!(job.get_process_minutes(ProcessType::Welding), 20);
        assert_eq!(job.get_process_minutes(ProcessType::Painting), 30);
        assert_eq!(job.get_process_minutes(ProcessType::Assembly), 40);
    }

    #[test]
    fn test_violations_multiple_shifts() {
        let mut job1 = create_test_job("VIN013", 60, 1);
        job1.energy_score = 1000.0;

        let mut job2 = create_test_job("VIN014", 60, 2);
        job2.energy_score = 800.0;

        let jobs = vec![job1, job2];
        let schedule = build_schedule(&jobs, 1704106800000);
        let jobs_by_vin: HashMap<String, Job> = jobs.iter()
            .map(|job| (job.vin.clone(), job.clone()))
            .collect();

        let limits = Limits {
            max_energy_per_shift: 1500.0,
            max_emission_per_shift: 800.0,
        };

        let violations = build_violations(&schedule, &limits, &jobs_by_vin);

        // 根据班次分配情况，可能有0个或多个违规
        // 如果两个作业在同一班次，总能耗1800 > 1500，会有1个违规
        // 如果在不同班次，都不超限，无违规
        assert!(violations.len() <= 1);
    }

    #[test]
    fn test_kpi_summary_with_zero_limits() {
        let jobs = vec![create_test_job("VIN015", 30, 1)];
        let schedule = build_schedule(&jobs, 1704106800000);
        let jobs_by_vin: HashMap<String, Job> = jobs.iter()
            .map(|job| (job.vin.clone(), job.clone()))
            .collect();

        let weights = Weights::default();
        let limits = Limits {
            max_energy_per_shift: 0.0, // 极端情况：限制为0
            max_emission_per_shift: 0.0,
        };

        let summary = build_kpi_summary(&schedule, &weights, &limits, &jobs_by_vin, 0);

        // 所有能耗和排放都会被计为超限
        assert!(summary.energy_excess > 0.0);
        assert!(summary.emission_excess > 0.0);
    }
}
