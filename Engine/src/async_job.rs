use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use std::time::{SystemTime, UNIX_EPOCH};
use uuid::Uuid;
use crate::model::SolveOutput;  // ✅ 删除未使用的 SolveInput
use crate::gen::aps::{SolveRequest, SolveResponse, KpiSummary, ScheduleItem, ShiftViolation, ConvergencePoint};

/// 任务状态枚举
#[derive(Debug, Clone)]
pub enum JobStatus {
    Queued,
    Running,
    Completed(SolveOutput),
    Failed(String),
}

/// 异步任务信息
#[derive(Debug, Clone)]
pub struct AsyncJob {
    pub job_id: String,
    pub status: JobStatus,
    pub created_at: u64,
    pub updated_at: u64,
    pub request: SolveRequest,
}

/// 任务管理器
#[derive(Debug, Default)]
pub struct JobManager {
    jobs: Arc<Mutex<HashMap<String, AsyncJob>>>,
}

impl JobManager {
    pub fn new() -> Self {
        Self {
            jobs: Arc::new(Mutex::new(HashMap::new())),
        }
    }

    /// 创建新任务
    pub fn create_job(&self, request: SolveRequest) -> String {
        let job_id = Uuid::new_v4().to_string();
        let now = SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_secs();

        let job = AsyncJob {
            job_id: job_id.clone(),
            status: JobStatus::Queued,
            created_at: now,
            updated_at: now,
            request,
        };

        let mut jobs = self.jobs.lock().unwrap();
        jobs.insert(job_id.clone(), job);

        job_id
    }

    /// 获取任务状态
    pub fn get_job(&self, job_id: &str) -> Option<AsyncJob> {
        let jobs = self.jobs.lock().unwrap();
        jobs.get(job_id).cloned()
    }

    /// 更新任务状态
    pub fn update_job(&self, job_id: &str, status: JobStatus) -> bool {
        let mut jobs = self.jobs.lock().unwrap();
        if let Some(job) = jobs.get_mut(job_id) {
            job.status = status;
            job.updated_at = SystemTime::now()
                .duration_since(UNIX_EPOCH)
                .unwrap()
                .as_secs();
            true
        } else {
            false
        }
    }

    /// 清理过期任务（可选）
    pub fn cleanup_old_jobs(&self, max_age_hours: u64) {
        let now = SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_secs();

        let max_age_seconds = max_age_hours * 3600;

        let mut jobs = self.jobs.lock().unwrap();
        jobs.retain(|_, job| {
            now.saturating_sub(job.updated_at) < max_age_seconds
        });
    }

    /// 获取所有任务
    pub fn list_jobs(&self) -> Vec<AsyncJob> {
        let jobs = self.jobs.lock().unwrap();
        jobs.values().cloned().collect()
    }
}

/// 将 SolveOutput 转换为 SolveResponse
impl From<SolveOutput> for SolveResponse {
    fn from(output: SolveOutput) -> Self {
        SolveResponse {
            request_id: output.request_id,
            summary: Some(KpiSummary {
                cost: output.summary.cost,
                total_tardiness_min: output.summary.total_tardiness_min,
                max_tardiness_min: output.summary.max_tardiness_min,
                color_changes: output.summary.color_changes,
                config_changes: output.summary.config_changes,
                energy_excess: output.summary.energy_excess,
                emission_excess: output.summary.emission_excess,
                material_shortage: output.summary.material_shortage,
                elapsed_ms: output.summary.elapsed_ms,
            }),
            order: output.order,
            // ✅ 修复：添加新增的 3 个字段
            schedule: output.schedule.into_iter().map(|item| ScheduleItem {
                seq: item.seq,
                vin: item.vin,
                start_epoch_ms: item.start_epoch_ms,
                end_epoch_ms: item.end_epoch_ms,
                due_epoch_ms: item.due_epoch_ms,
                tardiness_min: item.tardiness_min,
                color: item.color,
                config: item.config,
                shift_id: item.shift_id,
                // ✅ 新增字段
                process_type: item.process_type,
                line_id: item.line_id,
                seq_in_shift: item.seq_in_shift,
            }).collect(),
            detailed_schedule: output.detailed_schedule.into_iter().map(|item| ScheduleItem {
                seq: item.seq,
                vin: item.vin,
                start_epoch_ms: item.start_epoch_ms,
                end_epoch_ms: item.end_epoch_ms,
                due_epoch_ms: item.due_epoch_ms,
                tardiness_min: item.tardiness_min,
                color: item.color,
                config: item.config,
                shift_id: item.shift_id,
                // ✅ 新增字段
                process_type: item.process_type,
                line_id: item.line_id,
                seq_in_shift: item.seq_in_shift,
            }).collect(),
            violations: output.violations.into_iter().map(|v| ShiftViolation {
                shift_id: v.shift_id,
                vtype: v.vtype,
                excess: v.excess,
            }).collect(),
            baseline_summary: Some(KpiSummary {
                cost: output.baseline_summary.cost,
                total_tardiness_min: output.baseline_summary.total_tardiness_min,
                max_tardiness_min: output.baseline_summary.max_tardiness_min,
                color_changes: output.baseline_summary.color_changes,
                config_changes: output.baseline_summary.config_changes,
                energy_excess: output.baseline_summary.energy_excess,
                emission_excess: output.baseline_summary.emission_excess,
                material_shortage: output.baseline_summary.material_shortage,
                elapsed_ms: output.baseline_summary.elapsed_ms,
            }),
            convergence: output.convergence.into_iter().map(|c| ConvergencePoint {
                t_ms: c.t_ms,
                best_cost: c.best_cost,
            }).collect(),
            engine_version: "aps-engine/0.1.0".to_string(),
            warnings: Vec::new(),
        }
    }
}
