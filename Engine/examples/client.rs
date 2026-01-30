// examples/client.rs
use aps_engine::gen::aps::{
    aps_service_client::ApsServiceClient,
    SolveRequest, SolveParams, Weights, Limits, Job,
};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    println!("🚀 连接 gRPC 服务...");

    // 连接到 gRPC 服务
    let mut client = ApsServiceClient::connect("http://localhost:50051").await?;

    println!("✅ 连接成功！");

    // 构造请求
    let request = tonic::Request::new(SolveRequest {
        request_id: "client-test-001".to_string(),
        plan_start_epoch_ms: 1704106800000,
        jobs: vec![
            Job {
                vin: "CLIENT001".to_string(),
                due_epoch_ms: 1704110400000,
                color: "RED".to_string(),
                config: "BASE".to_string(),
                assemble_minutes: 60,
                stamping_minutes: 0,
                welding_minutes: 0,
                painting_minutes: 0,
                energy_score: 100.0,
                emission_score: 50.0,
                mold_code: String::new(),
                welding_fixture: String::new(),
            },
            Job {
                vin: "CLIENT002".to_string(),
                due_epoch_ms: 1704114000000,
                color: "BLUE".to_string(),
                config: "PREMIUM".to_string(),
                assemble_minutes: 45,
                stamping_minutes: 0,
                welding_minutes: 0,
                painting_minutes: 0,
                energy_score: 80.0,
                emission_score: 40.0,
                mold_code: String::new(),
                welding_fixture: String::new(),
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

    println!("\n📤 发送 Solve 请求...");

    // 调用 gRPC
    let response = client.solve(request).await?;
    let result = response.into_inner();

    println!("\n✅ 收到响应！");
    println!("   Request ID: {}", result.request_id);

    if let Some(summary) = result.summary {
        println!("   成本: {:.2}", summary.cost);
        println!("   颜色切换: {}", summary.color_changes);
        println!("   配置切换: {}", summary.config_changes);
        println!("   耗时: {}ms", summary.elapsed_ms);
    }

    println!("   排序: {:?}", result.order);
    println!("   引擎版本: {}", result.engine_version);

    println!("\n🎉 RPC 调用成功！");

    Ok(())
}
