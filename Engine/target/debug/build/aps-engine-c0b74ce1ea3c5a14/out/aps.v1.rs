/// ✅ 扩展 Job 消息（支持四大工艺）
#[allow(clippy::derive_partial_eq_without_eq)]
#[derive(Clone, PartialEq, ::prost::Message)]
pub struct Job {
    #[prost(string, tag = "1")]
    pub vin: ::prost::alloc::string::String,
    #[prost(int64, tag = "2")]
    pub due_epoch_ms: i64,
    #[prost(int32, tag = "3")]
    pub stamping_minutes: i32,
    #[prost(int32, tag = "4")]
    pub welding_minutes: i32,
    #[prost(int32, tag = "5")]
    pub painting_minutes: i32,
    #[prost(int32, tag = "6")]
    pub assemble_minutes: i32,
    #[prost(string, tag = "7")]
    pub mold_code: ::prost::alloc::string::String,
    #[prost(string, tag = "8")]
    pub welding_fixture: ::prost::alloc::string::String,
    #[prost(string, tag = "9")]
    pub color: ::prost::alloc::string::String,
    #[prost(string, tag = "10")]
    pub config: ::prost::alloc::string::String,
    #[prost(double, tag = "11")]
    pub energy_score: f64,
    #[prost(double, tag = "12")]
    pub emission_score: f64,
}
#[allow(clippy::derive_partial_eq_without_eq)]
#[derive(Clone, PartialEq, ::prost::Message)]
pub struct Weights {
    #[prost(double, tag = "1")]
    pub tardiness: f64,
    #[prost(double, tag = "2")]
    pub color_change: f64,
    #[prost(double, tag = "3")]
    pub config_change: f64,
    #[prost(double, tag = "4")]
    pub energy_excess: f64,
    #[prost(double, tag = "5")]
    pub emission_excess: f64,
    #[prost(double, tag = "6")]
    pub material_shortage: f64,
}
#[allow(clippy::derive_partial_eq_without_eq)]
#[derive(Clone, PartialEq, ::prost::Message)]
pub struct Limits {
    #[prost(double, tag = "1")]
    pub max_energy_per_shift: f64,
    #[prost(double, tag = "2")]
    pub max_emission_per_shift: f64,
}
#[allow(clippy::derive_partial_eq_without_eq)]
#[derive(Clone, PartialEq, ::prost::Message)]
pub struct SolveParams {
    #[prost(string, tag = "1")]
    pub algorithm: ::prost::alloc::string::String,
    #[prost(int32, tag = "2")]
    pub time_budget_sec: i32,
    #[prost(int64, tag = "3")]
    pub seed: i64,
    #[prost(message, optional, tag = "4")]
    pub weights: ::core::option::Option<Weights>,
    #[prost(message, optional, tag = "5")]
    pub limits: ::core::option::Option<Limits>,
}
#[allow(clippy::derive_partial_eq_without_eq)]
#[derive(Clone, PartialEq, ::prost::Message)]
pub struct ShiftViolation {
    #[prost(string, tag = "1")]
    pub shift_id: ::prost::alloc::string::String,
    /// ENERGY/EMISSION/MATERIAL
    #[prost(string, tag = "2")]
    pub vtype: ::prost::alloc::string::String,
    #[prost(double, tag = "3")]
    pub excess: f64,
}
#[allow(clippy::derive_partial_eq_without_eq)]
#[derive(Clone, PartialEq, ::prost::Message)]
pub struct KpiSummary {
    #[prost(double, tag = "1")]
    pub cost: f64,
    #[prost(int64, tag = "2")]
    pub total_tardiness_min: i64,
    #[prost(int64, tag = "3")]
    pub max_tardiness_min: i64,
    #[prost(int32, tag = "4")]
    pub color_changes: i32,
    #[prost(int32, tag = "5")]
    pub config_changes: i32,
    #[prost(double, tag = "6")]
    pub energy_excess: f64,
    #[prost(double, tag = "7")]
    pub emission_excess: f64,
    #[prost(double, tag = "8")]
    pub material_shortage: f64,
    #[prost(int64, tag = "9")]
    pub elapsed_ms: i64,
}
/// ✅ 扩展 ScheduleItem 消息
#[allow(clippy::derive_partial_eq_without_eq)]
#[derive(Clone, PartialEq, ::prost::Message)]
pub struct ScheduleItem {
    #[prost(int32, tag = "1")]
    pub seq: i32,
    #[prost(string, tag = "2")]
    pub vin: ::prost::alloc::string::String,
    #[prost(int32, tag = "3")]
    pub process_type: i32,
    #[prost(int64, tag = "4")]
    pub line_id: i64,
    #[prost(int32, tag = "5")]
    pub seq_in_shift: i32,
    #[prost(int64, tag = "6")]
    pub start_epoch_ms: i64,
    #[prost(int64, tag = "7")]
    pub end_epoch_ms: i64,
    #[prost(int64, tag = "8")]
    pub due_epoch_ms: i64,
    #[prost(int64, tag = "9")]
    pub tardiness_min: i64,
    #[prost(string, tag = "10")]
    pub color: ::prost::alloc::string::String,
    #[prost(string, tag = "11")]
    pub config: ::prost::alloc::string::String,
    #[prost(string, tag = "12")]
    pub shift_id: ::prost::alloc::string::String,
}
#[allow(clippy::derive_partial_eq_without_eq)]
#[derive(Clone, PartialEq, ::prost::Message)]
pub struct ConvergencePoint {
    #[prost(int64, tag = "1")]
    pub t_ms: i64,
    #[prost(double, tag = "2")]
    pub best_cost: f64,
}
#[allow(clippy::derive_partial_eq_without_eq)]
#[derive(Clone, PartialEq, ::prost::Message)]
pub struct SubmitJobRequest {
    #[prost(message, optional, tag = "1")]
    pub request: ::core::option::Option<SolveRequest>,
}
#[allow(clippy::derive_partial_eq_without_eq)]
#[derive(Clone, PartialEq, ::prost::Message)]
pub struct SubmitJobResponse {
    #[prost(string, tag = "1")]
    pub job_id: ::prost::alloc::string::String,
    #[prost(string, tag = "2")]
    pub message: ::prost::alloc::string::String,
}
#[allow(clippy::derive_partial_eq_without_eq)]
#[derive(Clone, PartialEq, ::prost::Message)]
pub struct GetJobStatusRequest {
    #[prost(string, tag = "1")]
    pub job_id: ::prost::alloc::string::String,
}
#[allow(clippy::derive_partial_eq_without_eq)]
#[derive(Clone, PartialEq, ::prost::Message)]
pub struct GetJobStatusResponse {
    #[prost(string, tag = "1")]
    pub job_id: ::prost::alloc::string::String,
    /// QUEUED, RUNNING, COMPLETED, FAILED
    #[prost(string, tag = "2")]
    pub status: ::prost::alloc::string::String,
    #[prost(int64, tag = "3")]
    pub created_at: i64,
    #[prost(int64, tag = "4")]
    pub updated_at: i64,
    #[prost(message, optional, tag = "5")]
    pub result: ::core::option::Option<SolveResponse>,
    #[prost(string, tag = "6")]
    pub error_message: ::prost::alloc::string::String,
    #[prost(string, tag = "100")]
    pub engine_version: ::prost::alloc::string::String,
}
#[allow(clippy::derive_partial_eq_without_eq)]
#[derive(Clone, PartialEq, ::prost::Message)]
pub struct ListJobsRequest {
    /// 0 表示不限制
    #[prost(int32, tag = "1")]
    pub limit: i32,
}
#[allow(clippy::derive_partial_eq_without_eq)]
#[derive(Clone, PartialEq, ::prost::Message)]
pub struct ListJobsResponse {
    #[prost(message, repeated, tag = "1")]
    pub jobs: ::prost::alloc::vec::Vec<JobInfo>,
}
#[allow(clippy::derive_partial_eq_without_eq)]
#[derive(Clone, PartialEq, ::prost::Message)]
pub struct JobInfo {
    #[prost(string, tag = "1")]
    pub job_id: ::prost::alloc::string::String,
    #[prost(string, tag = "2")]
    pub status: ::prost::alloc::string::String,
    #[prost(int64, tag = "3")]
    pub created_at: i64,
    #[prost(int64, tag = "4")]
    pub updated_at: i64,
}
#[allow(clippy::derive_partial_eq_without_eq)]
#[derive(Clone, PartialEq, ::prost::Message)]
pub struct SolveRequest {
    #[prost(string, tag = "1")]
    pub request_id: ::prost::alloc::string::String,
    #[prost(int64, tag = "2")]
    pub plan_start_epoch_ms: i64,
    #[prost(message, repeated, tag = "3")]
    pub jobs: ::prost::alloc::vec::Vec<Job>,
    #[prost(message, optional, tag = "4")]
    pub params: ::core::option::Option<SolveParams>,
}
#[allow(clippy::derive_partial_eq_without_eq)]
#[derive(Clone, PartialEq, ::prost::Message)]
pub struct SolveResponse {
    #[prost(string, tag = "1")]
    pub request_id: ::prost::alloc::string::String,
    #[prost(message, optional, tag = "2")]
    pub summary: ::core::option::Option<KpiSummary>,
    #[prost(message, optional, tag = "3")]
    pub baseline_summary: ::core::option::Option<KpiSummary>,
    #[prost(string, repeated, tag = "4")]
    pub order: ::prost::alloc::vec::Vec<::prost::alloc::string::String>,
    #[prost(message, repeated, tag = "5")]
    pub schedule: ::prost::alloc::vec::Vec<ScheduleItem>,
    #[prost(message, repeated, tag = "6")]
    pub detailed_schedule: ::prost::alloc::vec::Vec<ScheduleItem>,
    #[prost(message, repeated, tag = "7")]
    pub violations: ::prost::alloc::vec::Vec<ShiftViolation>,
    #[prost(message, repeated, tag = "8")]
    pub convergence: ::prost::alloc::vec::Vec<ConvergencePoint>,
    #[prost(string, tag = "9")]
    pub engine_version: ::prost::alloc::string::String,
    #[prost(string, repeated, tag = "10")]
    pub warnings: ::prost::alloc::vec::Vec<::prost::alloc::string::String>,
}
/// Generated client implementations.
pub mod aps_service_client {
    #![allow(unused_variables, dead_code, missing_docs, clippy::let_unit_value)]
    use tonic::codegen::*;
    use tonic::codegen::http::Uri;
    #[derive(Debug, Clone)]
    pub struct ApsServiceClient<T> {
        inner: tonic::client::Grpc<T>,
    }
    impl ApsServiceClient<tonic::transport::Channel> {
        /// Attempt to create a new client by connecting to a given endpoint.
        pub async fn connect<D>(dst: D) -> Result<Self, tonic::transport::Error>
        where
            D: TryInto<tonic::transport::Endpoint>,
            D::Error: Into<StdError>,
        {
            let conn = tonic::transport::Endpoint::new(dst)?.connect().await?;
            Ok(Self::new(conn))
        }
    }
    impl<T> ApsServiceClient<T>
    where
        T: tonic::client::GrpcService<tonic::body::BoxBody>,
        T::Error: Into<StdError>,
        T::ResponseBody: Body<Data = Bytes> + Send + 'static,
        <T::ResponseBody as Body>::Error: Into<StdError> + Send,
    {
        pub fn new(inner: T) -> Self {
            let inner = tonic::client::Grpc::new(inner);
            Self { inner }
        }
        pub fn with_origin(inner: T, origin: Uri) -> Self {
            let inner = tonic::client::Grpc::with_origin(inner, origin);
            Self { inner }
        }
        pub fn with_interceptor<F>(
            inner: T,
            interceptor: F,
        ) -> ApsServiceClient<InterceptedService<T, F>>
        where
            F: tonic::service::Interceptor,
            T::ResponseBody: Default,
            T: tonic::codegen::Service<
                http::Request<tonic::body::BoxBody>,
                Response = http::Response<
                    <T as tonic::client::GrpcService<tonic::body::BoxBody>>::ResponseBody,
                >,
            >,
            <T as tonic::codegen::Service<
                http::Request<tonic::body::BoxBody>,
            >>::Error: Into<StdError> + Send + Sync,
        {
            ApsServiceClient::new(InterceptedService::new(inner, interceptor))
        }
        /// Compress requests with the given encoding.
        ///
        /// This requires the server to support it otherwise it might respond with an
        /// error.
        #[must_use]
        pub fn send_compressed(mut self, encoding: CompressionEncoding) -> Self {
            self.inner = self.inner.send_compressed(encoding);
            self
        }
        /// Enable decompressing responses.
        #[must_use]
        pub fn accept_compressed(mut self, encoding: CompressionEncoding) -> Self {
            self.inner = self.inner.accept_compressed(encoding);
            self
        }
        /// Limits the maximum size of a decoded message.
        ///
        /// Default: `4MB`
        #[must_use]
        pub fn max_decoding_message_size(mut self, limit: usize) -> Self {
            self.inner = self.inner.max_decoding_message_size(limit);
            self
        }
        /// Limits the maximum size of an encoded message.
        ///
        /// Default: `usize::MAX`
        #[must_use]
        pub fn max_encoding_message_size(mut self, limit: usize) -> Self {
            self.inner = self.inner.max_encoding_message_size(limit);
            self
        }
        pub async fn solve(
            &mut self,
            request: impl tonic::IntoRequest<super::SolveRequest>,
        ) -> std::result::Result<tonic::Response<super::SolveResponse>, tonic::Status> {
            self.inner
                .ready()
                .await
                .map_err(|e| {
                    tonic::Status::new(
                        tonic::Code::Unknown,
                        format!("Service was not ready: {}", e.into()),
                    )
                })?;
            let codec = tonic::codec::ProstCodec::default();
            let path = http::uri::PathAndQuery::from_static("/aps.v1.ApsService/Solve");
            let mut req = request.into_request();
            req.extensions_mut().insert(GrpcMethod::new("aps.v1.ApsService", "Solve"));
            self.inner.unary(req, path, codec).await
        }
        pub async fn submit_job(
            &mut self,
            request: impl tonic::IntoRequest<super::SubmitJobRequest>,
        ) -> std::result::Result<
            tonic::Response<super::SubmitJobResponse>,
            tonic::Status,
        > {
            self.inner
                .ready()
                .await
                .map_err(|e| {
                    tonic::Status::new(
                        tonic::Code::Unknown,
                        format!("Service was not ready: {}", e.into()),
                    )
                })?;
            let codec = tonic::codec::ProstCodec::default();
            let path = http::uri::PathAndQuery::from_static(
                "/aps.v1.ApsService/SubmitJob",
            );
            let mut req = request.into_request();
            req.extensions_mut()
                .insert(GrpcMethod::new("aps.v1.ApsService", "SubmitJob"));
            self.inner.unary(req, path, codec).await
        }
        pub async fn get_job_status(
            &mut self,
            request: impl tonic::IntoRequest<super::GetJobStatusRequest>,
        ) -> std::result::Result<
            tonic::Response<super::GetJobStatusResponse>,
            tonic::Status,
        > {
            self.inner
                .ready()
                .await
                .map_err(|e| {
                    tonic::Status::new(
                        tonic::Code::Unknown,
                        format!("Service was not ready: {}", e.into()),
                    )
                })?;
            let codec = tonic::codec::ProstCodec::default();
            let path = http::uri::PathAndQuery::from_static(
                "/aps.v1.ApsService/GetJobStatus",
            );
            let mut req = request.into_request();
            req.extensions_mut()
                .insert(GrpcMethod::new("aps.v1.ApsService", "GetJobStatus"));
            self.inner.unary(req, path, codec).await
        }
        pub async fn list_jobs(
            &mut self,
            request: impl tonic::IntoRequest<super::ListJobsRequest>,
        ) -> std::result::Result<
            tonic::Response<super::ListJobsResponse>,
            tonic::Status,
        > {
            self.inner
                .ready()
                .await
                .map_err(|e| {
                    tonic::Status::new(
                        tonic::Code::Unknown,
                        format!("Service was not ready: {}", e.into()),
                    )
                })?;
            let codec = tonic::codec::ProstCodec::default();
            let path = http::uri::PathAndQuery::from_static(
                "/aps.v1.ApsService/ListJobs",
            );
            let mut req = request.into_request();
            req.extensions_mut()
                .insert(GrpcMethod::new("aps.v1.ApsService", "ListJobs"));
            self.inner.unary(req, path, codec).await
        }
    }
}
/// Generated server implementations.
pub mod aps_service_server {
    #![allow(unused_variables, dead_code, missing_docs, clippy::let_unit_value)]
    use tonic::codegen::*;
    /// Generated trait containing gRPC methods that should be implemented for use with ApsServiceServer.
    #[async_trait]
    pub trait ApsService: Send + Sync + 'static {
        async fn solve(
            &self,
            request: tonic::Request<super::SolveRequest>,
        ) -> std::result::Result<tonic::Response<super::SolveResponse>, tonic::Status>;
        async fn submit_job(
            &self,
            request: tonic::Request<super::SubmitJobRequest>,
        ) -> std::result::Result<
            tonic::Response<super::SubmitJobResponse>,
            tonic::Status,
        >;
        async fn get_job_status(
            &self,
            request: tonic::Request<super::GetJobStatusRequest>,
        ) -> std::result::Result<
            tonic::Response<super::GetJobStatusResponse>,
            tonic::Status,
        >;
        async fn list_jobs(
            &self,
            request: tonic::Request<super::ListJobsRequest>,
        ) -> std::result::Result<
            tonic::Response<super::ListJobsResponse>,
            tonic::Status,
        >;
    }
    #[derive(Debug)]
    pub struct ApsServiceServer<T: ApsService> {
        inner: _Inner<T>,
        accept_compression_encodings: EnabledCompressionEncodings,
        send_compression_encodings: EnabledCompressionEncodings,
        max_decoding_message_size: Option<usize>,
        max_encoding_message_size: Option<usize>,
    }
    struct _Inner<T>(Arc<T>);
    impl<T: ApsService> ApsServiceServer<T> {
        pub fn new(inner: T) -> Self {
            Self::from_arc(Arc::new(inner))
        }
        pub fn from_arc(inner: Arc<T>) -> Self {
            let inner = _Inner(inner);
            Self {
                inner,
                accept_compression_encodings: Default::default(),
                send_compression_encodings: Default::default(),
                max_decoding_message_size: None,
                max_encoding_message_size: None,
            }
        }
        pub fn with_interceptor<F>(
            inner: T,
            interceptor: F,
        ) -> InterceptedService<Self, F>
        where
            F: tonic::service::Interceptor,
        {
            InterceptedService::new(Self::new(inner), interceptor)
        }
        /// Enable decompressing requests with the given encoding.
        #[must_use]
        pub fn accept_compressed(mut self, encoding: CompressionEncoding) -> Self {
            self.accept_compression_encodings.enable(encoding);
            self
        }
        /// Compress responses with the given encoding, if the client supports it.
        #[must_use]
        pub fn send_compressed(mut self, encoding: CompressionEncoding) -> Self {
            self.send_compression_encodings.enable(encoding);
            self
        }
        /// Limits the maximum size of a decoded message.
        ///
        /// Default: `4MB`
        #[must_use]
        pub fn max_decoding_message_size(mut self, limit: usize) -> Self {
            self.max_decoding_message_size = Some(limit);
            self
        }
        /// Limits the maximum size of an encoded message.
        ///
        /// Default: `usize::MAX`
        #[must_use]
        pub fn max_encoding_message_size(mut self, limit: usize) -> Self {
            self.max_encoding_message_size = Some(limit);
            self
        }
    }
    impl<T, B> tonic::codegen::Service<http::Request<B>> for ApsServiceServer<T>
    where
        T: ApsService,
        B: Body + Send + 'static,
        B::Error: Into<StdError> + Send + 'static,
    {
        type Response = http::Response<tonic::body::BoxBody>;
        type Error = std::convert::Infallible;
        type Future = BoxFuture<Self::Response, Self::Error>;
        fn poll_ready(
            &mut self,
            _cx: &mut Context<'_>,
        ) -> Poll<std::result::Result<(), Self::Error>> {
            Poll::Ready(Ok(()))
        }
        fn call(&mut self, req: http::Request<B>) -> Self::Future {
            let inner = self.inner.clone();
            match req.uri().path() {
                "/aps.v1.ApsService/Solve" => {
                    #[allow(non_camel_case_types)]
                    struct SolveSvc<T: ApsService>(pub Arc<T>);
                    impl<T: ApsService> tonic::server::UnaryService<super::SolveRequest>
                    for SolveSvc<T> {
                        type Response = super::SolveResponse;
                        type Future = BoxFuture<
                            tonic::Response<Self::Response>,
                            tonic::Status,
                        >;
                        fn call(
                            &mut self,
                            request: tonic::Request<super::SolveRequest>,
                        ) -> Self::Future {
                            let inner = Arc::clone(&self.0);
                            let fut = async move { (*inner).solve(request).await };
                            Box::pin(fut)
                        }
                    }
                    let accept_compression_encodings = self.accept_compression_encodings;
                    let send_compression_encodings = self.send_compression_encodings;
                    let max_decoding_message_size = self.max_decoding_message_size;
                    let max_encoding_message_size = self.max_encoding_message_size;
                    let inner = self.inner.clone();
                    let fut = async move {
                        let inner = inner.0;
                        let method = SolveSvc(inner);
                        let codec = tonic::codec::ProstCodec::default();
                        let mut grpc = tonic::server::Grpc::new(codec)
                            .apply_compression_config(
                                accept_compression_encodings,
                                send_compression_encodings,
                            )
                            .apply_max_message_size_config(
                                max_decoding_message_size,
                                max_encoding_message_size,
                            );
                        let res = grpc.unary(method, req).await;
                        Ok(res)
                    };
                    Box::pin(fut)
                }
                "/aps.v1.ApsService/SubmitJob" => {
                    #[allow(non_camel_case_types)]
                    struct SubmitJobSvc<T: ApsService>(pub Arc<T>);
                    impl<
                        T: ApsService,
                    > tonic::server::UnaryService<super::SubmitJobRequest>
                    for SubmitJobSvc<T> {
                        type Response = super::SubmitJobResponse;
                        type Future = BoxFuture<
                            tonic::Response<Self::Response>,
                            tonic::Status,
                        >;
                        fn call(
                            &mut self,
                            request: tonic::Request<super::SubmitJobRequest>,
                        ) -> Self::Future {
                            let inner = Arc::clone(&self.0);
                            let fut = async move { (*inner).submit_job(request).await };
                            Box::pin(fut)
                        }
                    }
                    let accept_compression_encodings = self.accept_compression_encodings;
                    let send_compression_encodings = self.send_compression_encodings;
                    let max_decoding_message_size = self.max_decoding_message_size;
                    let max_encoding_message_size = self.max_encoding_message_size;
                    let inner = self.inner.clone();
                    let fut = async move {
                        let inner = inner.0;
                        let method = SubmitJobSvc(inner);
                        let codec = tonic::codec::ProstCodec::default();
                        let mut grpc = tonic::server::Grpc::new(codec)
                            .apply_compression_config(
                                accept_compression_encodings,
                                send_compression_encodings,
                            )
                            .apply_max_message_size_config(
                                max_decoding_message_size,
                                max_encoding_message_size,
                            );
                        let res = grpc.unary(method, req).await;
                        Ok(res)
                    };
                    Box::pin(fut)
                }
                "/aps.v1.ApsService/GetJobStatus" => {
                    #[allow(non_camel_case_types)]
                    struct GetJobStatusSvc<T: ApsService>(pub Arc<T>);
                    impl<
                        T: ApsService,
                    > tonic::server::UnaryService<super::GetJobStatusRequest>
                    for GetJobStatusSvc<T> {
                        type Response = super::GetJobStatusResponse;
                        type Future = BoxFuture<
                            tonic::Response<Self::Response>,
                            tonic::Status,
                        >;
                        fn call(
                            &mut self,
                            request: tonic::Request<super::GetJobStatusRequest>,
                        ) -> Self::Future {
                            let inner = Arc::clone(&self.0);
                            let fut = async move {
                                (*inner).get_job_status(request).await
                            };
                            Box::pin(fut)
                        }
                    }
                    let accept_compression_encodings = self.accept_compression_encodings;
                    let send_compression_encodings = self.send_compression_encodings;
                    let max_decoding_message_size = self.max_decoding_message_size;
                    let max_encoding_message_size = self.max_encoding_message_size;
                    let inner = self.inner.clone();
                    let fut = async move {
                        let inner = inner.0;
                        let method = GetJobStatusSvc(inner);
                        let codec = tonic::codec::ProstCodec::default();
                        let mut grpc = tonic::server::Grpc::new(codec)
                            .apply_compression_config(
                                accept_compression_encodings,
                                send_compression_encodings,
                            )
                            .apply_max_message_size_config(
                                max_decoding_message_size,
                                max_encoding_message_size,
                            );
                        let res = grpc.unary(method, req).await;
                        Ok(res)
                    };
                    Box::pin(fut)
                }
                "/aps.v1.ApsService/ListJobs" => {
                    #[allow(non_camel_case_types)]
                    struct ListJobsSvc<T: ApsService>(pub Arc<T>);
                    impl<
                        T: ApsService,
                    > tonic::server::UnaryService<super::ListJobsRequest>
                    for ListJobsSvc<T> {
                        type Response = super::ListJobsResponse;
                        type Future = BoxFuture<
                            tonic::Response<Self::Response>,
                            tonic::Status,
                        >;
                        fn call(
                            &mut self,
                            request: tonic::Request<super::ListJobsRequest>,
                        ) -> Self::Future {
                            let inner = Arc::clone(&self.0);
                            let fut = async move { (*inner).list_jobs(request).await };
                            Box::pin(fut)
                        }
                    }
                    let accept_compression_encodings = self.accept_compression_encodings;
                    let send_compression_encodings = self.send_compression_encodings;
                    let max_decoding_message_size = self.max_decoding_message_size;
                    let max_encoding_message_size = self.max_encoding_message_size;
                    let inner = self.inner.clone();
                    let fut = async move {
                        let inner = inner.0;
                        let method = ListJobsSvc(inner);
                        let codec = tonic::codec::ProstCodec::default();
                        let mut grpc = tonic::server::Grpc::new(codec)
                            .apply_compression_config(
                                accept_compression_encodings,
                                send_compression_encodings,
                            )
                            .apply_max_message_size_config(
                                max_decoding_message_size,
                                max_encoding_message_size,
                            );
                        let res = grpc.unary(method, req).await;
                        Ok(res)
                    };
                    Box::pin(fut)
                }
                _ => {
                    Box::pin(async move {
                        Ok(
                            http::Response::builder()
                                .status(200)
                                .header("grpc-status", "12")
                                .header("content-type", "application/grpc")
                                .body(empty_body())
                                .unwrap(),
                        )
                    })
                }
            }
        }
    }
    impl<T: ApsService> Clone for ApsServiceServer<T> {
        fn clone(&self) -> Self {
            let inner = self.inner.clone();
            Self {
                inner,
                accept_compression_encodings: self.accept_compression_encodings,
                send_compression_encodings: self.send_compression_encodings,
                max_decoding_message_size: self.max_decoding_message_size,
                max_encoding_message_size: self.max_encoding_message_size,
            }
        }
    }
    impl<T: ApsService> Clone for _Inner<T> {
        fn clone(&self) -> Self {
            Self(Arc::clone(&self.0))
        }
    }
    impl<T: std::fmt::Debug> std::fmt::Debug for _Inner<T> {
        fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
            write!(f, "{:?}", self.0)
        }
    }
    impl<T: ApsService> tonic::server::NamedService for ApsServiceServer<T> {
        const NAME: &'static str = "aps.v1.ApsService";
    }
}
