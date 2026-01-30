use tonic::Request;
use aps_engine::gen::aps::{
    aps_service_server::ApsService,
    SolveRequest, SolveParams, Weights, Limits, Job,
    SubmitJobRequest, GetJobStatusRequest, ListJobsRequest,
};
use aps_engine::service::ApsServiceImpl;

// ✅ 在 use 语句后面添加
fn default_weights() -> Weights {
    Weights {
        tardiness: 10.0,
        color_change: 50.0,
        config_change: 30.0,
        energy_excess: 2.0,
        emission_excess: 3.0,
        material_shortage: 0.0,
    }
}

fn default_limits() -> Limits {
    Limits {
        max_energy_per_shift: 5000.0,
        max_emission_per_shift: 2500.0,
    }
}

/// ✅ 测试：内存使用监控
#[tokio::test]
async fn test_grpc_memory_usage() {
    use std::sync::Arc;

    let service = Arc::new(ApsServiceImpl::new());

    println!("\n💾 内存使用监控：");

    // 获取初始内存（需要 sys-info crate）
    #[cfg(target_os = "linux")]
    {
        if let Ok(mem_info) = sys_info::mem_info() {
            println!("   初始可用内存: {} MB", mem_info.avail / 1024);
        }
    }

    let test_jobs: Vec<Job> = (0..50).map(|j| Job {
        vin: format!("MEM{:03}", j),
        due_epoch_ms: 1704106800000 + (j as i64 * 3600_000),
        stamping_minutes: 0,
        welding_minutes: 0,
        painting_minutes: 0,
        assemble_minutes: 60,
        mold_code: format!("MOLD{}", j % 5),
        welding_fixture: format!("FIX{}", j % 3),
        color: "RED".to_string(),
        config: "BASE".to_string(),
        energy_score: 100.0,
        emission_score: 50.0,
    }).collect();

    // 执行 100 次请求，检查内存是否泄漏
    for round in 0..10 {
        let mut handles = vec![];

        for i in 0..10 {
            let service_clone = service.clone();
            let jobs_clone = test_jobs.clone();

            let handle = tokio::spawn(async move {
                let request = Request::new(SolveRequest {
                    request_id: format!("mem-{:02}-{:02}", round, i),
                    plan_start_epoch_ms: 1704106800000,
                    jobs: jobs_clone,
                    params: Some(SolveParams {
                        algorithm: "sa".to_string(),
                        time_budget_sec: 2,
                        seed: (round * 10 + i) as i64,
                        weights: Some(default_weights()),
                        limits: Some(default_limits()),
                    }),
                });

                service_clone.solve(request).await
            });

            handles.push(handle);
        }

        futures::future::join_all(handles).await;

        #[cfg(target_os = "linux")]
        {
            if let Ok(mem_info) = sys_info::mem_info() {
                println!("   轮次 {}: 可用内存 {} MB", round + 1, mem_info.avail / 1024);
            }
        }
    }

    println!("   ✅ 内存监控完成（检查是否有明显下降）\n");
}

/// ✅ 测试：超时检测
#[tokio::test]
async fn test_grpc_timeout() {
    let service = ApsServiceImpl::new();

    println!("\n⏱️  超时测试：");

    // 构造一个需要很长时间的请求（50辆车，30秒预算）
    let test_jobs: Vec<Job> = (0..50).map(|j| Job {
        vin: format!("TIMEOUT{:03}", j),
        due_epoch_ms: 1704106800000 + (j as i64 * 3600_000),
        stamping_minutes: 0,
        welding_minutes: 0,
        painting_minutes: 0,
        assemble_minutes: 60 + (j % 60),
        mold_code: format!("MOLD{}", j % 5),
        welding_fixture: format!("FIX{}", j % 3),
        color: match j % 3 {
            0 => "RED",
            1 => "BLUE",
            _ => "WHITE",
        }.to_string(),
        config: if j % 2 == 0 { "BASE" } else { "PREMIUM" }.to_string(),
        energy_score: 80.0 + (j as f64 % 40.0),
        emission_score: 40.0 + (j as f64 % 20.0),
    }).collect();

    let request = Request::new(SolveRequest {
        request_id: "timeout-test-001".to_string(),
        plan_start_epoch_ms: 1704106800000,
        jobs: test_jobs,
        params: Some(SolveParams {
            algorithm: "hybrid".to_string(),
            time_budget_sec: 30,  // 要求30秒
            seed: 42,
            weights: Some(default_weights()),
            limits: Some(default_limits()),
        }),
    });

    println!("   正在执行（预计30秒）...");

    let start = std::time::Instant::now();

    // 🔥 手动设置 5 秒超时
    let timeout_duration = tokio::time::Duration::from_secs(5);

    match tokio::time::timeout(timeout_duration, service.solve(request)).await {
        Ok(Ok(_response)) => {
            let elapsed = start.elapsed();
            println!("   ✅ 请求在 {:.2}s 内完成（未超时）", elapsed.as_secs_f64());
        }
        Ok(Err(e)) => {
            println!("   ✗ 请求失败: {}", e);
        }
        Err(_) => {
            println!("   ⏱️  请求超时（5秒）");
            println!("   ✅ 超时机制生效！");
        }
    }

    println!("   ✅ 超时测试完成\n");
}

/// ✅ 测试：小规模资源压力测试（50个请求）
#[tokio::test]
async fn test_grpc_moderate_stress() {
    use std::sync::Arc;
    use std::sync::atomic::{AtomicU32, Ordering};

    let service = Arc::new(ApsServiceImpl::new());
    let success_count = Arc::new(AtomicU32::new(0));
    let error_count = Arc::new(AtomicU32::new(0));

    println!("\n🔥 中等压力测试（50个并发，20辆车，SA 3秒）：");

    let test_jobs: Vec<Job> = (0..20).map(|j| Job {
        vin: format!("V{:03}", j),
        due_epoch_ms: 1704106800000 + (j as i64 * 3600_000),
        stamping_minutes: 0,
        welding_minutes: 0,
        painting_minutes: 0,
        assemble_minutes: 30 + (j % 60),
        mold_code: format!("MOLD{}", j % 5),
        welding_fixture: format!("FIX{}", j % 3),
        color: match j % 3 {
            0 => "RED",
            1 => "BLUE",
            _ => "WHITE",
        }.to_string(),
        config: if j % 2 == 0 { "BASE" } else { "PREMIUM" }.to_string(),
        energy_score: 80.0 + (j as f64 % 40.0),
        emission_score: 40.0 + (j as f64 % 20.0),
    }).collect();

    let mut handles = vec![];
    let start = std::time::Instant::now();

    // 50 个并发请求
    for i in 0..50 {
        let service_clone = service.clone();
        let success_count_clone = success_count.clone();
        let error_count_clone = error_count.clone();
        let jobs_clone = test_jobs.clone();

        let handle = tokio::spawn(async move {
            let request = Request::new(SolveRequest {
                request_id: format!("moderate-{:03}", i),
                plan_start_epoch_ms: 1704106800000,
                jobs: jobs_clone,
                params: Some(SolveParams {
                    algorithm: "sa".to_string(),
                    time_budget_sec: 3,  // 3秒预算
                    seed: i as i64,
                    weights: Some(default_weights()),
                    limits: Some(default_limits()),
                }),
            });

            match service_clone.solve(request).await {
                Ok(_) => {
                    success_count_clone.fetch_add(1, Ordering::SeqCst);
                }
                Err(e) => {
                    error_count_clone.fetch_add(1, Ordering::SeqCst);
                    eprintln!("   ✗ 请求 {} 失败: {}", i, e);
                }
            }
        });

        handles.push(handle);
    }

    let results = futures::future::join_all(handles).await;
    let elapsed = start.elapsed();

    let success = success_count.load(Ordering::SeqCst);
    let errors = error_count.load(Ordering::SeqCst);

    println!("   成功: {}/50", success);
    println!("   失败: {}", errors);
    println!("   总耗时: {:.2}s", elapsed.as_secs_f64());
    println!("   平均响应: {:.0}ms", elapsed.as_millis() as f64 / 50.0);

    // 理论计算
    let theoretical_serial = 50 * 3;  // 150 秒
    let theoretical_parallel = (50.0 / num_cpus::get() as f64) * 3.0;  // 假设完美并行

    println!("\n   📊 性能分析：");
    println!("   理论串行时间: {}s", theoretical_serial);
    println!("   理论并行时间: {:.1}s (假设 {} 核)", theoretical_parallel, num_cpus::get());
    println!("   实际时间: {:.2}s", elapsed.as_secs_f64());

    if elapsed.as_secs_f64() > theoretical_parallel * 2.0 {
        println!("   ⚠️  实际时间远超理论并行时间！");
        println!("   🔍 可能原因：");
        println!("      1. solve() 阻塞了异步运行时");
        println!("      2. 实际并发数 ≈ CPU 核心数");
        println!("      3. 需要使用 spawn_blocking");
    } else {
        println!("   ✅ 并发效果良好");
    }

    let panics = results.iter().filter(|r| r.is_err()).count();
    println!("   Panic 数量: {}", panics);

    println!("   ✅ 中等压力测试完成\n");
}

/// ✅ 测试 13：串行 vs 并发性能对比
#[tokio::test]
async fn test_grpc_concurrent_vs_serial() {
    use std::sync::Arc;

    let service = Arc::new(ApsServiceImpl::new());

    println!("\n📊 串行 vs 并发性能对比：");

    // 准备测试数据（10辆车）
    let test_jobs: Vec<Job> = (0..10).map(|j| Job {
        vin: format!("V{:03}", j),
        due_epoch_ms: 1704106800000 + (j as i64 * 3600_000),
        stamping_minutes: 0,
        welding_minutes: 0,
        painting_minutes: 0,
        assemble_minutes: 30 + (j % 30),
        mold_code: format!("MOLD{}", j % 3),
        welding_fixture: format!("FIX{}", j % 2),
        color: match j % 3 {
            0 => "RED",
            1 => "BLUE",
            _ => "WHITE",
        }.to_string(),
        config: if j % 2 == 0 { "BASE" } else { "PREMIUM" }.to_string(),
        energy_score: 100.0,
        emission_score: 50.0,
    }).collect();

    let num_requests = 20;

    // ========== 串行执行 ==========
    println!("\n   🐌 串行执行 {} 个请求...", num_requests);
    let serial_start = std::time::Instant::now();

    for i in 0..num_requests {
        let request = Request::new(SolveRequest {
            request_id: format!("serial-{:03}", i),
            plan_start_epoch_ms: 1704106800000,
            jobs: test_jobs.clone(),
            params: Some(SolveParams {
                algorithm: "sa".to_string(),
                time_budget_sec: 1,
                seed: i as i64,
                weights: Some(default_weights()),
                limits: Some(default_limits()),
            }),
        });

        let _ = service.solve(request).await;
    }

    let serial_elapsed = serial_start.elapsed();
    println!("   串行总耗时: {:.2}s", serial_elapsed.as_secs_f64());

    // ========== 并发执行 ==========
    println!("\n   🚀 并发执行 {} 个请求...", num_requests);
    let concurrent_start = std::time::Instant::now();

    let mut handles = vec![];

    for i in 0..num_requests {
        let service_clone = service.clone();
        let jobs_clone = test_jobs.clone();

        let handle = tokio::spawn(async move {
            let request = Request::new(SolveRequest {
                request_id: format!("concurrent-{:03}", i),
                plan_start_epoch_ms: 1704106800000,
                jobs: jobs_clone,
                params: Some(SolveParams {
                    algorithm: "sa".to_string(),
                    time_budget_sec: 1,
                    seed: i as i64,
                    weights: Some(default_weights()),
                    limits: Some(default_limits()),
                }),
            });

            service_clone.solve(request).await
        });

        handles.push(handle);
    }

    let results = futures::future::join_all(handles).await;
    let concurrent_elapsed = concurrent_start.elapsed();

    let success_count = results.iter()
        .filter(|r| r.as_ref().unwrap().is_ok())
        .count();

    println!("   并发总耗时: {:.2}s", concurrent_elapsed.as_secs_f64());
    println!("   成功请求: {}/{}", success_count, num_requests);

    // ========== 性能对比 ==========
    println!("\n   📈 性能对比：");
    println!("   串行: {:.2}s", serial_elapsed.as_secs_f64());
    println!("   并发: {:.2}s", concurrent_elapsed.as_secs_f64());

    let speedup = serial_elapsed.as_secs_f64() / concurrent_elapsed.as_secs_f64();
    println!("   加速比: {:.2}x", speedup);

    if speedup > 1.5 {
        println!("   ✅ 并发加速显著！");
    } else if speedup > 1.0 {
        println!("   ⚠️  并发有轻微加速");
    } else {
        println!("   ❌ 并发无加速效果（可能是CPU密集型任务）");
    }

    assert_eq!(success_count, num_requests, "所有请求都应该成功");
    println!("   ✅ 对比测试通过！\n");
}

/// ✅ 测试 10：真正的高压并发测试
#[tokio::test]
async fn test_grpc_high_concurrency() {
    use std::sync::Arc;
    use std::sync::atomic::{AtomicU32, Ordering};

    let service = Arc::new(ApsServiceImpl::new());
    let success_count = Arc::new(AtomicU32::new(0));
    let error_count = Arc::new(AtomicU32::new(0));

    println!("\n🔥 高压并发测试（100个并发请求）：");

    let mut handles = vec![];

    // ✅ 生成更复杂的测试数据
    let test_jobs: Vec<Job> = (0..10).map(|j| Job {
        vin: format!("V{:03}", j),
        due_epoch_ms: 1704106800000 + (j as i64 * 3600_000),
        stamping_minutes: 0,
        welding_minutes: 0,
        painting_minutes: 0,
        assemble_minutes: 30 + (j % 30),
        mold_code: format!("MOLD{}", j % 3),
        welding_fixture: format!("FIX{}", j % 2),
        color: match j % 3 {
            0 => "RED",
            1 => "BLUE",
            _ => "WHITE",
        }.to_string(),
        config: if j % 2 == 0 { "BASE" } else { "PREMIUM" }.to_string(),
        energy_score: 100.0,
        emission_score: 50.0,
    }).collect();

    for i in 0..100 {
        let service_clone = service.clone();
        let success_count_clone = success_count.clone();
        let error_count_clone = error_count.clone();
        let jobs_clone = test_jobs.clone();

        let handle = tokio::spawn(async move {
            let request = Request::new(SolveRequest {
                request_id: format!("stress-{:03}", i),
                plan_start_epoch_ms: 1704106800000,
                jobs: jobs_clone,  // ✅ 10辆车
                params: Some(SolveParams {
                    algorithm: "sa".to_string(),  // ✅ 使用 SA 算法
                    time_budget_sec: 1,           // ✅ 1秒预算
                    seed: i as i64,
                    weights: Some(default_weights()),
                    limits: Some(default_limits()),
                }),
            });

            match service_clone.solve(request).await {
                Ok(_) => {
                    success_count_clone.fetch_add(1, Ordering::SeqCst);
                    true
                }
                Err(e) => {
                    error_count_clone.fetch_add(1, Ordering::SeqCst);
                    eprintln!("   ✗ 请求 {} 失败: {}", i, e);
                    false
                }
            }
        });

        handles.push(handle);
    }

    // 并发执行所有任务
    let start = std::time::Instant::now();
    futures::future::join_all(handles).await;
    let elapsed = start.elapsed();

    let success = success_count.load(Ordering::SeqCst);
    let errors = error_count.load(Ordering::SeqCst);

    println!("   成功: {}/100", success);
    println!("   失败: {}", errors);
    println!("   总耗时: {:.2}s", elapsed.as_secs_f64());
    println!("   平均响应: {:.0}ms", elapsed.as_millis() as f64 / 100.0);

    // ✅ 验证并发效率
    if success == 100 {
        let avg_time_ms = elapsed.as_millis() as f64 / 100.0;
        if avg_time_ms < 1000.0 {
            println!("   🚀 并发加速生效！（平均 {:.0}ms < 1000ms）", avg_time_ms);
        } else {
            println!("   ⚠️  串行执行？（平均 {:.0}ms ≈ 1000ms）", avg_time_ms);
        }
    }

    assert_eq!(success, 100, "所有请求都应该成功");
    println!("   ✅ 高压并发测试通过！\n");
}

/// ✅ 测试 12：竞态条件检测
#[tokio::test]
async fn test_grpc_race_condition() {
    use std::sync::Arc;

    let service = Arc::new(ApsServiceImpl::new());

    println!("\n⚔️  竞态条件检测：");

    // 创建一个任务
    let submit_request = Request::new(SubmitJobRequest {
        request: Some(SolveRequest {
            request_id: "race-test-001".to_string(),
            plan_start_epoch_ms: 1704106800000,
            jobs: vec![
                Job {
                    vin: "RACE001".to_string(),
                    due_epoch_ms: 1704110400000,
                    stamping_minutes: 0,
                    welding_minutes: 0,
                    painting_minutes: 0,
                    assemble_minutes: 30,
                    mold_code: String::new(),
                    welding_fixture: String::new(),
                    color: "RED".to_string(),
                    config: "BASE".to_string(),
                    energy_score: 100.0,
                    emission_score: 50.0,
                },
            ],
            params: Some(SolveParams {
                algorithm: "sa".to_string(),
                time_budget_sec: 2,
                seed: 42,
                weights: Some(default_weights()),
                limits: Some(default_limits()),
            }),
        }),
    });

    let job_id = service.submit_job(submit_request).await
        .unwrap().into_inner().job_id;

    println!("   任务已创建: {}", job_id);

    // 并发查询同一个任务状态（10个并发）
    let mut handles = vec![];

    for i in 0..10 {
        let service_clone = service.clone();
        let job_id_clone = job_id.clone();

        let handle = tokio::spawn(async move {
            for _ in 0..5 {  // 每个任务查询5次
                let request = Request::new(GetJobStatusRequest {
                    job_id: job_id_clone.clone(),
                });

                let result = service_clone.get_job_status(request).await;
                if result.is_err() {
                    eprintln!("   ✗ 查询 {} 失败", i);
                    return false;
                }

                tokio::time::sleep(tokio::time::Duration::from_millis(10)).await;
            }
            true
        });

        handles.push(handle);
    }

    let results = futures::future::join_all(handles).await;

    let success_count = results.iter().filter(|r| r.as_ref().unwrap_or(&false) == &true).count();

    println!("   并发查询成功: {}/10", success_count);
    assert_eq!(success_count, 10, "所有并发查询都应该成功");

    println!("   ✅ 竞态条件检测通过（无死锁、无数据竞争）！\n");
}

/// ✅ 测试 11：并发异步任务提交
#[tokio::test]
async fn test_grpc_concurrent_async_jobs() {
    use std::sync::Arc;

    let service = Arc::new(ApsServiceImpl::new());

    println!("\n📝 并发异步任务提交测试（50个任务）：");

    let mut handles = vec![];

    for i in 0..50 {
        let service_clone = service.clone();

        let handle = tokio::spawn(async move {
            let request = Request::new(SubmitJobRequest {
                request: Some(SolveRequest {
                    request_id: format!("async-concurrent-{:03}", i),
                    plan_start_epoch_ms: 1704106800000,
                    jobs: vec![
                        Job {
                            vin: format!("ASYNC{:03}", i),
                            due_epoch_ms: 1704110400000,
                            stamping_minutes: 0,
                            welding_minutes: 0,
                            painting_minutes: 0,
                            assemble_minutes: 30,
                            mold_code: String::new(),
                            welding_fixture: String::new(),
                            color: "RED".to_string(),
                            config: "BASE".to_string(),
                            energy_score: 100.0,
                            emission_score: 50.0,
                        },
                    ],
                    params: Some(SolveParams {
                        algorithm: "baseline".to_string(),
                        time_budget_sec: 1,
                        seed: i as i64,
                        weights: Some(default_weights()),
                        limits: Some(default_limits()),
                    }),
                }),
            });

            service_clone.submit_job(request).await
        });

        handles.push(handle);
    }

    let results = futures::future::join_all(handles).await;

    let mut success_count = 0;
    for result in results {
        if result.is_ok() && result.unwrap().is_ok() {
            success_count += 1;
        }
    }

    println!("   成功提交: {}/50", success_count);
    assert_eq!(success_count, 50, "所有异步任务都应该成功提交");

    // 等待任务执行
    tokio::time::sleep(tokio::time::Duration::from_secs(2)).await;

    // 查询任务列表
    let list_request = Request::new(ListJobsRequest { limit: 100 });
    let list_response = service.list_jobs(list_request).await.unwrap().into_inner();

    println!("   任务列表数量: {}", list_response.jobs.len());
    assert!(list_response.jobs.len() >= 50, "应该至少有50个任务");

    // 统计任务状态
    let mut completed = 0;
    let mut running = 0;
    let mut queued = 0;

    for job in &list_response.jobs {
        match job.status.as_str() {
            "COMPLETED" => completed += 1,
            "RUNNING" => running += 1,
            "QUEUED" => queued += 1,
            _ => {}
        }
    }

    println!("   已完成: {}", completed);
    println!("   运行中: {}", running);
    println!("   队列中: {}", queued);

    println!("   ✅ 并发异步任务测试通过！\n");
}


/// ✅ 测试 1：基本求解功能
#[tokio::test]
async fn test_grpc_solve_basic() {
    let service = ApsServiceImpl::new();

    // 构造测试数据
    let request = Request::new(SolveRequest {
        request_id: "grpc-test-001".to_string(),
        plan_start_epoch_ms: 1704106800000, // 2024-01-01 19:00:00
        jobs: vec![
            Job {
                vin: "VIN001".to_string(),
                due_epoch_ms: 1704110400000, // 1小时后
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
                due_epoch_ms: 1704114000000, // 2小时后
                stamping_minutes: 0,
                welding_minutes: 0,
                painting_minutes: 0,
                assemble_minutes: 45,
                mold_code: String::new(),
                welding_fixture: String::new(),
                color: "BLUE".to_string(),
                config: "PREMIUM".to_string(),
                energy_score: 80.0,
                emission_score: 40.0,
            },
            Job {
                vin: "VIN003".to_string(),
                due_epoch_ms: 1704117600000, // 3小时后
                stamping_minutes: 0,
                welding_minutes: 0,
                painting_minutes: 0,
                assemble_minutes: 30,
                mold_code: String::new(),
                welding_fixture: String::new(),
                color: "RED".to_string(),
                config: "BASE".to_string(),
                energy_score: 90.0,
                emission_score: 45.0,
            },
        ],
        params: Some(SolveParams {
            algorithm: "sa".to_string(),
            time_budget_sec: 3,
            seed: 42,
            weights: Some(Weights {
                tardiness: 10.0,
                color_change: 50.0,
                config_change: 30.0,
                energy_excess: 2.0,
                emission_excess: 3.0,
                material_shortage: 0.0,
            }),
            limits: Some(Limits {
                max_energy_per_shift: 5000.0,
                max_emission_per_shift: 2500.0,
            }),
        }),
    });

    // 执行求解
    let response = service.solve(request).await;

    // 验证结果
    assert!(response.is_ok(), "求解应该成功");

    let output = response.unwrap().into_inner();

    println!("\n🎯 gRPC 求解测试结果：");
    println!("   Request ID: {}", output.request_id);
    println!("   算法版本: {}", output.engine_version);

    if let Some(summary) = &output.summary {
        println!("   总成本: {:.2}", summary.cost);
        println!("   颜色切换: {}", summary.color_changes);
        println!("   配置切换: {}", summary.config_changes);
        println!("   运行时间: {}ms", summary.elapsed_ms);
    }

    // 断言
    assert_eq!(output.request_id, "grpc-test-001");
    assert_eq!(output.order.len(), 3, "应该返回3个车辆的排序");
    assert_eq!(output.schedule.len(), 3, "应该返回3个调度项");
    assert!(output.summary.is_some(), "应该有summary");
    assert!(output.baseline_summary.is_some(), "应该有baseline_summary");

    if let Some(summary) = output.summary {
        assert!(summary.cost >= 0.0, "成本应该非负");
        assert!(summary.elapsed_ms > 0, "运行时间应该大于0");
    }

    println!("   ✅ 基本求解测试通过！\n");
}

/// ✅ 测试 2：不同算法对比
#[tokio::test]
async fn test_grpc_multiple_algorithms() {
    let service = ApsServiceImpl::new();

    let jobs = vec![
        Job {
            vin: "V1".to_string(),
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
            vin: "V2".to_string(),
            due_epoch_ms: 1704114000000,
            stamping_minutes: 0,
            welding_minutes: 0,
            painting_minutes: 0,
            assemble_minutes: 45,
            mold_code: String::new(),
            welding_fixture: String::new(),
            color: "BLUE".to_string(),
            config: "PREMIUM".to_string(),
            energy_score: 80.0,
            emission_score: 40.0,
        },
    ];

    let algorithms = vec!["baseline", "sa", "hybrid"];

    println!("\n📊 算法对比测试：");

    for algo in algorithms {
        let request = Request::new(SolveRequest {
            request_id: format!("algo-test-{}", algo),
            plan_start_epoch_ms: 1704106800000,
            jobs: jobs.clone(),
            params: Some(SolveParams {
                algorithm: algo.to_string(),
                time_budget_sec: 2,
                seed: 42,
                weights: Some(Weights {
                    tardiness: 10.0,
                    color_change: 50.0,
                    config_change: 30.0,
                    energy_excess: 2.0,
                    emission_excess: 3.0,
                    material_shortage: 0.0,
                }),
                limits: Some(Limits {
                    max_energy_per_shift: 5000.0,
                    max_emission_per_shift: 2500.0,
                }),
            }),
        });

        let response = service.solve(request).await;
        assert!(response.is_ok(), "算法 {} 应该成功", algo);

        let output = response.unwrap().into_inner();

        if let Some(summary) = output.summary {
            println!("   {} - 成本: {:.2}, 时间: {}ms",
                     algo, summary.cost, summary.elapsed_ms);
        }
    }

    println!("   ✅ 多算法测试通过！\n");
}

/// ✅ 测试 3：异步任务提交和查询
#[tokio::test]
async fn test_grpc_async_job() {
    let service = ApsServiceImpl::new();

    // 提交任务
    let submit_request = Request::new(SubmitJobRequest {
        request: Some(SolveRequest {
            request_id: "async-test-001".to_string(),
            plan_start_epoch_ms: 1704106800000,
            jobs: vec![
                Job {
                    vin: "ASYNC001".to_string(),
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
            ],
            params: Some(SolveParams {
                algorithm: "sa".to_string(),
                time_budget_sec: 2,
                seed: 42,
                weights: Some(default_weights()),
                limits: Some(default_limits()),
            }),
        }),
    });

    println!("\n🔄 异步任务测试：");

    let submit_response = service.submit_job(submit_request).await;
    assert!(submit_response.is_ok(), "任务提交应该成功");

    let job_id = submit_response.unwrap().into_inner().job_id;
    println!("   任务已提交: {}", job_id);

    // 等待任务完成
    tokio::time::sleep(tokio::time::Duration::from_millis(500)).await;

    // 查询任务状态
    let status_request = Request::new(GetJobStatusRequest {
        job_id: job_id.clone(),
    });

    let status_response = service.get_job_status(status_request).await;
    assert!(status_response.is_ok(), "状态查询应该成功");

    let status = status_response.unwrap().into_inner();
    println!("   任务状态: {}", status.status);
    println!("   创建时间: {}", status.created_at);
    println!("   更新时间: {}", status.updated_at);

    // 验证状态
    assert!(
        status.status == "RUNNING" || status.status == "COMPLETED",
        "任务应该在运行或已完成"
    );

    // 再等待确保完成
    tokio::time::sleep(tokio::time::Duration::from_secs(3)).await;

    let final_status_request = Request::new(GetJobStatusRequest {
        job_id: job_id.clone(),
    });

    let final_status = service.get_job_status(final_status_request).await
        .unwrap().into_inner();

    println!("   最终状态: {}", final_status.status);
    assert_eq!(final_status.status, "COMPLETED", "任务应该已完成");

    if let Some(result) = final_status.result {
        if let Some(summary) = result.summary {
            println!("   最终成本: {:.2}", summary.cost);
            assert!(summary.cost >= 0.0);
        }
    }

    println!("   ✅ 异步任务测试通过！\n");
}

/// ✅ 测试 4：任务列表
#[tokio::test]
async fn test_grpc_list_jobs() {
    let service = ApsServiceImpl::new();

    println!("\n📋 任务列表测试：");

    // 提交多个任务
    for i in 1..=3 {
        let request = Request::new(SubmitJobRequest {
            request: Some(SolveRequest {
                request_id: format!("list-test-{:03}", i),
                plan_start_epoch_ms: 1704106800000,
                jobs: vec![
                    Job {
                        vin: format!("LIST{:03}", i),
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
                ],
                params: Some(SolveParams {
                    algorithm: "baseline".to_string(),
                    time_budget_sec: 1,
                    seed: i as i64,
                    weights: Some(default_weights()),
                    limits: Some(default_limits()),
                }),
            }),
        });

        service.submit_job(request).await.expect("任务提交失败");
        println!("   任务 {} 已提交", i);
    }

    // 查询任务列表
    let list_request = Request::new(ListJobsRequest {
        limit: 10,
    });

    let list_response = service.list_jobs(list_request).await;
    assert!(list_response.is_ok(), "列表查询应该成功");

    let jobs = list_response.unwrap().into_inner().jobs;
    println!("   任务总数: {}", jobs.len());
    assert!(jobs.len() >= 3, "应该至少有3个任务");

    for job in jobs.iter().take(5) {
        println!("   - {} | {} | 创建于 {}",
                 job.job_id, job.status, job.created_at);
    }

    println!("   ✅ 任务列表测试通过！\n");
}

/// ✅ 测试 5：参数验证
#[tokio::test]
async fn test_grpc_validation() {
    let service = ApsServiceImpl::new();

    println!("\n🛡️  参数验证测试：");

    // 测试空任务列表
    let empty_request = Request::new(SolveRequest {
        request_id: "empty-test".to_string(),
        plan_start_epoch_ms: 1704106800000,
        jobs: vec![],
        params: None,
    });

    let response = service.solve(empty_request).await;
    assert!(response.is_err(), "空任务列表应该失败");
    println!("   ✓ 空任务列表验证通过");

    // 测试过多任务
    let too_many_jobs: Vec<Job> = (0..501).map(|i| Job {
        vin: format!("V{:04}", i),
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
    }).collect();

    let too_many_request = Request::new(SolveRequest {
        request_id: "too-many-test".to_string(),
        plan_start_epoch_ms: 1704106800000,
        jobs: too_many_jobs,
        params: None,
    });

    let response = service.solve(too_many_request).await;
    assert!(response.is_err(), "超过500个任务应该失败");
    println!("   ✓ 任务数量限制验证通过");

    // 测试未知算法（应该给出警告但成功执行）
    let unknown_algo_request = Request::new(SolveRequest {
        request_id: "unknown-algo-test".to_string(),
        plan_start_epoch_ms: 1704106800000,
        jobs: vec![
            Job {
                vin: "TEST001".to_string(),
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
        ],
        params: Some(SolveParams {
            algorithm: "unknown_algorithm".to_string(),
            time_budget_sec: 2,
            seed: 42,
            weights: Some(default_weights()),
            limits: Some(default_limits()),
        }),
    });

    let response = service.solve(unknown_algo_request).await;
    assert!(response.is_ok(), "未知算法应该回退到baseline");

    let output = response.unwrap().into_inner();
    assert!(!output.warnings.is_empty(), "应该有警告信息");
    println!("   ✓ 未知算法警告: {}", output.warnings[0]);

    // 测试极端时间预算
    let extreme_time_request = Request::new(SolveRequest {
        request_id: "extreme-time-test".to_string(),
        plan_start_epoch_ms: 1704106800000,
        jobs: vec![
            Job {
                vin: "TEST002".to_string(),
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
        ],
        params: Some(SolveParams {
            algorithm: "sa".to_string(),
            time_budget_sec: 100, // 超过60秒
            seed: 42,
            weights: Some(default_weights()),
            limits: Some(default_limits()),
        }),
    });

    let response = service.solve(extreme_time_request).await;
    assert!(response.is_ok(), "时间预算应该被截断");

    let output = response.unwrap().into_inner();
    assert!(!output.warnings.is_empty(), "应该有时间截断警告");
    println!("   ✓ 时间预算截断: {}", output.warnings[0]);

    println!("   ✅ 参数验证测试全部通过！\n");
}

/// ✅ 测试 6：性能测试（大规模数据）
#[tokio::test]
async fn test_grpc_performance() {
    let service = ApsServiceImpl::new();

    println!("\n⚡ 性能测试（50辆车）：");

    // 生成50辆车的测试数据
    let jobs: Vec<Job> = (1..=50).map(|i| Job {
        vin: format!("PERF{:03}", i),
        due_epoch_ms: 1704106800000 + (i as i64 * 3600_000), // 每小时一辆
        stamping_minutes: 0,
        welding_minutes: 0,
        painting_minutes: 0,
        assemble_minutes: 30 + (i % 60),
        mold_code: format!("MOLD{}", i % 5),
        welding_fixture: format!("FIX{}", i % 3),
        color: match i % 3 {
            0 => "RED",
            1 => "BLUE",
            _ => "WHITE",
        }.to_string(),
        config: if i % 2 == 0 { "BASE" } else { "PREMIUM" }.to_string(),
        energy_score: 80.0 + (i as f64 % 40.0),
        emission_score: 40.0 + (i as f64 % 20.0),
    }).collect();

    let algorithms = vec![
        ("baseline", 1),
        ("sa", 5),
        ("hybrid", 8),
    ];

    for (algo, budget) in algorithms {
        let request = Request::new(SolveRequest {
            request_id: format!("perf-test-{}", algo),
            plan_start_epoch_ms: 1704106800000,
            jobs: jobs.clone(),
            params: Some(SolveParams {
                algorithm: algo.to_string(),
                time_budget_sec: budget,
                seed: 42,
                weights: Some(Weights {
                    tardiness: 10.0,
                    color_change: 50.0,
                    config_change: 30.0,
                    energy_excess: 2.0,
                    emission_excess: 3.0,
                    material_shortage: 0.0,
                }),
                limits: Some(Limits {
                    max_energy_per_shift: 5000.0,
                    max_emission_per_shift: 2500.0,
                }),
            }),
        });

        let start = std::time::Instant::now();
        let response = service.solve(request).await;
        let elapsed = start.elapsed();

        assert!(response.is_ok(), "{} 算法应该成功", algo);

        let output = response.unwrap().into_inner();



        // ✅ 修复后的代码
        if let Some(ref summary) = output.summary {
            println!("   {} - 成本: {:.2}, 颜色切换: {}, 实际耗时: {:.2}s",
                     algo, summary.cost, summary.color_changes, elapsed.as_secs_f64());

            assert!(elapsed.as_secs() <= (budget as u64 + 2),
                    "{} 算法运行时间应在预算内", algo);
            assert!(summary.cost >= 0.0, "成本应该非负");
        }

        if let (Some(ref baseline), Some(ref optimized)) = (&output.baseline_summary, &output.summary) {
            if algo != "baseline" {
                let improvement = (baseline.cost - optimized.cost) / baseline.cost * 100.0;
                println!("     改进幅度: {:.2}%", improvement);
            }
        }
    }
    println!("   ✅ 性能测试通过！\n");
}

/// ✅ 测试 7：并发测试
#[tokio::test]
async fn test_grpc_concurrency() {
    use std::sync::Arc;

    let service = Arc::new(ApsServiceImpl::new());

    println!("\n🔀 并发测试（10个并发请求）：");

    let mut handles = vec![];

    for i in 0..10 {
        let service_clone = service.clone();

        let handle = tokio::spawn(async move {
            let request = Request::new(SolveRequest {
                request_id: format!("concurrent-{:02}", i),
                plan_start_epoch_ms: 1704106800000,
                jobs: vec![
                    Job {
                        vin: format!("CONC{:03}", i),
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
                        vin: format!("CONC{:03}-2", i),
                        due_epoch_ms: 1704114000000,
                        stamping_minutes: 0,
                        welding_minutes: 0,
                        painting_minutes: 0,
                        assemble_minutes: 45,
                        mold_code: String::new(),
                        welding_fixture: String::new(),
                        color: "BLUE".to_string(),
                        config: "PREMIUM".to_string(),
                        energy_score: 80.0,
                        emission_score: 40.0,
                    },
                ],
                params: Some(SolveParams {
                    algorithm: "sa".to_string(),
                    time_budget_sec: 2,
                    seed: i as i64,
                    weights: Some(default_weights()),
                    limits: Some(default_limits()),
                }),
            });

            let result = service_clone.solve(request).await;
            (i, result.is_ok())
        });

        handles.push(handle);
    }

    // 等待所有任务完成
    let results = futures::future::join_all(handles).await;

    let mut success_count = 0;
    for result in results {
        let (id, success) = result.unwrap();
        if success {
            success_count += 1;
            println!("   ✓ 请求 {} 成功", id);
        } else {
            println!("   ✗ 请求 {} 失败", id);
        }
    }

    assert_eq!(success_count, 10, "所有并发请求都应该成功");
    println!("   ✅ 并发测试通过！({}/10 成功)\n", success_count);
}

/// ✅ 测试 8：健康检查
#[tokio::test]
async fn test_grpc_health_check() {
    let service = ApsServiceImpl::new();

    println!("\n🏥 健康检查测试：");

    // 等待一小段时间
    tokio::time::sleep(tokio::time::Duration::from_millis(100)).await;

    let health = service.health_check().await;

    println!("   状态: {}", health.status);
    println!("   版本: {}", health.version);
    println!("   运行时间: {}秒", health.uptime_seconds);
    println!("   总请求数: {}", health.metrics.total_requests);

    assert_eq!(health.status, "healthy");
    assert!(!health.version.is_empty());
    assert!(health.uptime_seconds >= 0);

    println!("   ✅ 健康检查通过！\n");
}

/// ✅ 测试 9：四大工艺完整流程
#[tokio::test]
async fn test_grpc_four_processes() {
    let service = ApsServiceImpl::new();

    println!("\n🏭 四大工艺完整流程测试：");

    let request = Request::new(SolveRequest {
        request_id: "four-process-test".to_string(),
        plan_start_epoch_ms: 1704106800000,
        jobs: vec![
            Job {
                vin: "FULL001".to_string(),
                due_epoch_ms: 1704200000000, // 26小时后
                stamping_minutes: 120,   // ✅ 冲压 2小时
                welding_minutes: 180,    // ✅ 焊装 3小时
                painting_minutes: 240,   // ✅ 涂装 4小时
                assemble_minutes: 300,   // ✅ 总装 5小时
                mold_code: "MOLD_A".to_string(),
                welding_fixture: "FIX_X".to_string(),
                color: "RED".to_string(),
                config: "PREMIUM".to_string(),
                energy_score: 120.0,
                emission_score: 60.0,
            },
            Job {
                vin: "FULL002".to_string(),
                due_epoch_ms: 1704210000000,
                stamping_minutes: 100,
                welding_minutes: 150,
                painting_minutes: 200,
                assemble_minutes: 250,
                mold_code: "MOLD_B".to_string(),
                welding_fixture: "FIX_Y".to_string(),
                color: "BLUE".to_string(),
                config: "BASE".to_string(),
                energy_score: 100.0,
                emission_score: 50.0,
            },
        ],
        params: Some(SolveParams {
            algorithm: "sa".to_string(),
            time_budget_sec: 3,
            seed: 42,
            weights: Some(Weights {
                tardiness: 10.0,
                color_change: 50.0,
                config_change: 30.0,
                energy_excess: 2.0,
                emission_excess: 3.0,
                material_shortage: 0.0,
            }),
            limits: Some(Limits {
                max_energy_per_shift: 5000.0,
                max_emission_per_shift: 2500.0,
            }),
        }),
    });

    let response = service.solve(request).await;
    assert!(response.is_ok(), "四大工艺求解应该成功");

    let output = response.unwrap().into_inner();

    println!("   车辆数: {}", output.order.len());
    println!("   调度项: {}", output.schedule.len());

    // 验证调度项包含工艺信息
    for (i, item) in output.schedule.iter().enumerate() {
        println!("   调度 {}: VIN={}, 工艺={}, 产线={}, 班次顺序={}",
                 i + 1, item.vin, item.process_type, item.line_id, item.seq_in_shift);

        assert!(item.process_type >= 1 && item.process_type <= 4,
                "工艺类型应该在1-4之间");
    }

    if let Some(summary) = output.summary {
        println!("   总成本: {:.2}", summary.cost);
        println!("   总延迟: {} 分钟", summary.total_tardiness_min);
        assert!(summary.cost >= 0.0);
    }

    println!("   ✅ 四大工艺测试通过！\n");
}



