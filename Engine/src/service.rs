use tonic::{Request, Response, Status};
use crate::gen::aps::aps_service_server::ApsService;
use crate::gen::aps::{
    SolveRequest, SolveResponse, SubmitJobRequest, SubmitJobResponse,
    GetJobStatusRequest, GetJobStatusResponse, ListJobsRequest, ListJobsResponse, JobInfo
};
use crate::model::SolveInput;
use crate::solve;  // ✅ 使用统一入口函数
use crate::async_job::{JobManager, JobStatus};
use crate::observability::{MetricsCollector, HealthResponse};
use std::sync::Arc;
use std::time::Instant;

pub struct ApsServiceImpl {
    job_manager: Arc<JobManager>,
    metrics: Arc<MetricsCollector>,
    start_time: Instant,
}

impl ApsServiceImpl {
    pub fn new() -> Self {
        Self {
            job_manager: Arc::new(JobManager::new()),
            metrics: Arc::new(MetricsCollector::new()),
            start_time: Instant::now(),
        }
    }

    pub async fn health_check(&self) -> HealthResponse {
        let mut metrics = self.metrics.get_metrics();
        metrics.uptime_seconds = self.start_time.elapsed().as_secs();
        HealthResponse::new(metrics)
    }
}

/// ✅ 辅助函数：将 gRPC Job 转换为内部 Job
fn convert_grpc_job_to_model(job: crate::gen::aps::Job) -> crate::model::Job {
    crate::model::Job {
        vin: job.vin,
        due_epoch_ms: job.due_epoch_ms,
        stamping_minutes: job.stamping_minutes,
        welding_minutes: job.welding_minutes,
        painting_minutes: job.painting_minutes,
        assemble_minutes: job.assemble_minutes,
        mold_code: job.mold_code,
        welding_fixture: job.welding_fixture,
        color: job.color,
        config: job.config,
        energy_score: job.energy_score,
        emission_score: job.emission_score,
    }
}

/// ✅ 辅助函数：将内部 ScheduleItem 转换为 gRPC ScheduleItem
fn convert_model_schedule_to_grpc(item: crate::model::ScheduleItem) -> crate::gen::aps::ScheduleItem {
    crate::gen::aps::ScheduleItem {
        seq: item.seq,
        vin: item.vin,
        process_type: item.process_type,
        line_id: item.line_id,
        seq_in_shift: item.seq_in_shift,
        start_epoch_ms: item.start_epoch_ms,
        end_epoch_ms: item.end_epoch_ms,
        due_epoch_ms: item.due_epoch_ms,
        tardiness_min: item.tardiness_min,
        color: item.color,
        config: item.config,
        shift_id: item.shift_id,
    }
}

#[tonic::async_trait]
impl ApsService for ApsServiceImpl {
    async fn solve(&self, request: Request<SolveRequest>) -> Result<Response<SolveResponse>, Status> {
        let start_time = Instant::now();
        let req = request.into_inner();

        // 参数验证
        if req.jobs.is_empty() {
            self.metrics.record_request(false, 0);
            return Err(Status::invalid_argument("Empty jobs list"));
        }

        if req.jobs.len() > 500 {
            self.metrics.record_request(false, 0);
            return Err(Status::invalid_argument("Too many jobs (max 500)"));
        }

        // ✅ 时间预算验证（带警告）
        let original_time_budget = req.params.as_ref()
            .map(|p| p.time_budget_sec)
            .unwrap_or(5);

        let mut time_budget_sec = original_time_budget;
        let mut warnings = Vec::new();

        if time_budget_sec <= 0 {
            warnings.push("Time budget <= 0, using default 5s".to_string());
            time_budget_sec = 5;
        } else if time_budget_sec > 60 {
            warnings.push(format!("Time budget {}s truncated to 60s", original_time_budget));
            time_budget_sec = 60;
        }

        // ✅ 构建输入
        let input = SolveInput {
            request_id: req.request_id.clone(),
            plan_start_epoch_ms: req.plan_start_epoch_ms,
            jobs: req.jobs.into_iter()
                .map(convert_grpc_job_to_model)
                .collect(),
            params: match req.params {
                Some(params) => crate::model::SolveParams {
                    algorithm: params.algorithm,
                    time_budget_sec,
                    seed: params.seed,
                    weights: params.weights.map(|w| crate::model::Weights {
                        tardiness: w.tardiness,
                        color_change: w.color_change,
                        config_change: w.config_change,
                        energy_excess: w.energy_excess,
                        emission_excess: w.emission_excess,
                        material_shortage: w.material_shortage,
                    }).unwrap_or_default(),
                    limits: params.limits.map(|l| crate::model::Limits {
                        max_energy_per_shift: l.max_energy_per_shift,
                        max_emission_per_shift: l.max_emission_per_shift,
                    }).unwrap_or_default(),
                },
                None => crate::model::SolveParams {
                    algorithm: "baseline".to_string(),
                    time_budget_sec,
                    seed: 42,
                    ..Default::default()
                },
            },
        };

        // ✅ 算法验证
        let algorithm = &input.params.algorithm;
        match algorithm.as_str() {
            "baseline" | "sa"  | "hybrid" => {},
            _ => {
                warnings.push(format!("Unknown algorithm '{}', falling back to baseline", algorithm));
            }
        }

        // ✅ 使用统一入口函数求解
        let output = solve(&input);

        let response_time_ms = start_time.elapsed().as_millis() as u64;
        self.metrics.record_request(true, response_time_ms);

        let response = SolveResponse {
            request_id: output.request_id,
            summary: Some(crate::gen::aps::KpiSummary {
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
            baseline_summary: Some(crate::gen::aps::KpiSummary {
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
            order: output.order,
            schedule: output.schedule.into_iter()
                .map(convert_model_schedule_to_grpc)
                .collect(),
            // ✅ 新增：详细工序调度
            detailed_schedule: output.detailed_schedule.into_iter()
                .map(convert_model_schedule_to_grpc)
                .collect(),
            violations: output.violations.into_iter().map(|v| crate::gen::aps::ShiftViolation {
                shift_id: v.shift_id,
                vtype: v.vtype,
                excess: v.excess,
            }).collect(),
            convergence: output.convergence.into_iter().map(|c| crate::gen::aps::ConvergencePoint {
                t_ms: c.t_ms,
                best_cost: c.best_cost,
            }).collect(),
            engine_version: env!("CARGO_PKG_VERSION").to_string(),
            warnings,
        };

        Ok(Response::new(response))
    }

    async fn submit_job(&self, request: Request<SubmitJobRequest>) -> Result<Response<SubmitJobResponse>, Status> {
        println!("submit_job request: {:?}", request);
        let req = request.into_inner();
        let solve_req = req.request.ok_or(Status::invalid_argument("Missing request"))?;

        // 参数验证
        if solve_req.jobs.is_empty() {
            return Err(Status::invalid_argument("Empty jobs list"));
        }

        if solve_req.jobs.len() > 500 {
            return Err(Status::invalid_argument("Too many jobs (max 500)"));
        }

        // ✅ 时间预算和算法验证
        let mut warnings = Vec::new();

        if let Some(params) = &solve_req.params {
            // 算法验证
            match params.algorithm.as_str() {
                "baseline" | "sa" |  "hybrid" => {},
                _ => warnings.push(format!("Unknown algorithm '{}', will use baseline", params.algorithm)),
            }

            // 时间预算验证
            if params.time_budget_sec <= 0 {
                warnings.push("Time budget <= 0, will use default 5s".to_string());
            } else if params.time_budget_sec > 60 {
                warnings.push(format!("Time budget {}s will be truncated to 60s", params.time_budget_sec));
            }
        }

        let job_id = self.job_manager.create_job(solve_req);

        // 创建后台任务
        let job_manager = self.job_manager.clone();
        let job_id_clone = job_id.clone();

        tokio::spawn(async move {
            job_manager.update_job(&job_id_clone, JobStatus::Running);

            if let Some(job) = job_manager.get_job(&job_id_clone) {
                // ✅ 构建输入
                let input = SolveInput {
                    request_id: job.request.request_id.clone(),
                    plan_start_epoch_ms: job.request.plan_start_epoch_ms,
                    jobs: job.request.jobs.iter()
                        .cloned()
                        .map(convert_grpc_job_to_model)
                        .collect(),
                    params: match &job.request.params {
                        Some(params) => crate::model::SolveParams {
                            algorithm: params.algorithm.clone(),
                            time_budget_sec: params.time_budget_sec.max(1).min(60),
                            seed: params.seed,
                            weights: params.weights.as_ref().map(|w| crate::model::Weights {
                                tardiness: w.tardiness,
                                color_change: w.color_change,
                                config_change: w.config_change,
                                energy_excess: w.energy_excess,
                                emission_excess: w.emission_excess,
                                material_shortage: w.material_shortage,
                            }).unwrap_or_default(),
                            limits: params.limits.as_ref().map(|l| crate::model::Limits {
                                max_energy_per_shift: l.max_energy_per_shift,
                                max_emission_per_shift: l.max_emission_per_shift,
                            }).unwrap_or_default(),
                        },
                        None => crate::model::SolveParams {
                            algorithm: "baseline".to_string(),
                            time_budget_sec: 5,
                            seed: 42,
                            ..Default::default()
                        },
                    },
                };

                // ✅ 使用统一入口函数求解
                let output = solve(&input);

                job_manager.update_job(&job_id_clone, JobStatus::Completed(output));
            }
        });

        Ok(Response::new(SubmitJobResponse {
            job_id,
            message: "Job submitted successfully".to_string(),
        }))
    }

    async fn get_job_status(&self, request: Request<GetJobStatusRequest>) -> Result<Response<GetJobStatusResponse>, Status> {
        println!("get_job_status request: {:?}", request);
        let req = request.into_inner();

        let job = self.job_manager.get_job(&req.job_id)
            .ok_or_else(|| Status::not_found(format!("Job {} not found", req.job_id)))?;

        let (status, result, error_message) = match job.status {
            JobStatus::Queued => ("QUEUED", None, None),
            JobStatus::Running => ("RUNNING", None, None),
            JobStatus::Completed(ref output) => ("COMPLETED", Some(output.clone().into()), None),
            JobStatus::Failed(ref error) => ("FAILED", None, Some(error.clone())),
        };

        Ok(Response::new(GetJobStatusResponse {
            job_id: job.job_id,
            status: status.to_string(),
            created_at: job.created_at as i64,
            updated_at: job.updated_at as i64,
            result,
            error_message: error_message.unwrap_or_default(),
            engine_version: env!("CARGO_PKG_VERSION").to_string(),
        }))
    }

    async fn list_jobs(&self, request: Request<ListJobsRequest>) -> Result<Response<ListJobsResponse>, Status> {
        println!("list_jobs request: {:?}", request);
        let req = request.into_inner();

        let mut jobs = self.job_manager.list_jobs();

        // 按更新时间倒序排序
        jobs.sort_by(|a, b| b.updated_at.cmp(&a.updated_at));

        // 限制数量
        if req.limit > 0 {
            jobs.truncate(req.limit as usize);
        }

        let job_infos: Vec<JobInfo> = jobs.into_iter().map(|job| {
            let status = match job.status {
                JobStatus::Queued => "QUEUED",
                JobStatus::Running => "RUNNING",
                JobStatus::Completed(_) => "COMPLETED",
                JobStatus::Failed(_) => "FAILED",
            };

            JobInfo {
                job_id: job.job_id,
                status: status.to_string(),
                created_at: job.created_at as i64,
                updated_at: job.updated_at as i64,
            }
        }).collect();

        Ok(Response::new(ListJobsResponse {
            jobs: job_infos,
        }))
    }
}

// ========== 默认实现 ==========
impl Default for ApsServiceImpl {
    fn default() -> Self {
        Self::new()
    }
}
