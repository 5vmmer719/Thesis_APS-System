use tonic::transport::Server;
use aps_engine::gen::aps::aps_service_server::ApsServiceServer;
use aps_engine::service::ApsServiceImpl;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let addr = "0.0.0.0:50051".parse()?;
    let service = ApsServiceImpl::new();

    println!("APS Engine gRPC server listening on {} with async support", addr);

    Server::builder()
        .add_service(ApsServiceServer::new(service))
        .serve(addr)
        .await?;

    Ok(())
}