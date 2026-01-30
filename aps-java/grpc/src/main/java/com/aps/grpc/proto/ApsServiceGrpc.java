package com.aps.grpc.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.61.0)",
    comments = "Source: aps.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class ApsServiceGrpc {

  private ApsServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "aps.v1.ApsService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.aps.grpc.proto.SolveRequest,
      com.aps.grpc.proto.SolveResponse> getSolveMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Solve",
      requestType = com.aps.grpc.proto.SolveRequest.class,
      responseType = com.aps.grpc.proto.SolveResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.aps.grpc.proto.SolveRequest,
      com.aps.grpc.proto.SolveResponse> getSolveMethod() {
    io.grpc.MethodDescriptor<com.aps.grpc.proto.SolveRequest, com.aps.grpc.proto.SolveResponse> getSolveMethod;
    if ((getSolveMethod = ApsServiceGrpc.getSolveMethod) == null) {
      synchronized (ApsServiceGrpc.class) {
        if ((getSolveMethod = ApsServiceGrpc.getSolveMethod) == null) {
          ApsServiceGrpc.getSolveMethod = getSolveMethod =
              io.grpc.MethodDescriptor.<com.aps.grpc.proto.SolveRequest, com.aps.grpc.proto.SolveResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Solve"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.aps.grpc.proto.SolveRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.aps.grpc.proto.SolveResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ApsServiceMethodDescriptorSupplier("Solve"))
              .build();
        }
      }
    }
    return getSolveMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.aps.grpc.proto.SubmitJobRequest,
      com.aps.grpc.proto.SubmitJobResponse> getSubmitJobMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SubmitJob",
      requestType = com.aps.grpc.proto.SubmitJobRequest.class,
      responseType = com.aps.grpc.proto.SubmitJobResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.aps.grpc.proto.SubmitJobRequest,
      com.aps.grpc.proto.SubmitJobResponse> getSubmitJobMethod() {
    io.grpc.MethodDescriptor<com.aps.grpc.proto.SubmitJobRequest, com.aps.grpc.proto.SubmitJobResponse> getSubmitJobMethod;
    if ((getSubmitJobMethod = ApsServiceGrpc.getSubmitJobMethod) == null) {
      synchronized (ApsServiceGrpc.class) {
        if ((getSubmitJobMethod = ApsServiceGrpc.getSubmitJobMethod) == null) {
          ApsServiceGrpc.getSubmitJobMethod = getSubmitJobMethod =
              io.grpc.MethodDescriptor.<com.aps.grpc.proto.SubmitJobRequest, com.aps.grpc.proto.SubmitJobResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SubmitJob"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.aps.grpc.proto.SubmitJobRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.aps.grpc.proto.SubmitJobResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ApsServiceMethodDescriptorSupplier("SubmitJob"))
              .build();
        }
      }
    }
    return getSubmitJobMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.aps.grpc.proto.GetJobStatusRequest,
      com.aps.grpc.proto.GetJobStatusResponse> getGetJobStatusMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetJobStatus",
      requestType = com.aps.grpc.proto.GetJobStatusRequest.class,
      responseType = com.aps.grpc.proto.GetJobStatusResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.aps.grpc.proto.GetJobStatusRequest,
      com.aps.grpc.proto.GetJobStatusResponse> getGetJobStatusMethod() {
    io.grpc.MethodDescriptor<com.aps.grpc.proto.GetJobStatusRequest, com.aps.grpc.proto.GetJobStatusResponse> getGetJobStatusMethod;
    if ((getGetJobStatusMethod = ApsServiceGrpc.getGetJobStatusMethod) == null) {
      synchronized (ApsServiceGrpc.class) {
        if ((getGetJobStatusMethod = ApsServiceGrpc.getGetJobStatusMethod) == null) {
          ApsServiceGrpc.getGetJobStatusMethod = getGetJobStatusMethod =
              io.grpc.MethodDescriptor.<com.aps.grpc.proto.GetJobStatusRequest, com.aps.grpc.proto.GetJobStatusResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetJobStatus"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.aps.grpc.proto.GetJobStatusRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.aps.grpc.proto.GetJobStatusResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ApsServiceMethodDescriptorSupplier("GetJobStatus"))
              .build();
        }
      }
    }
    return getGetJobStatusMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.aps.grpc.proto.ListJobsRequest,
      com.aps.grpc.proto.ListJobsResponse> getListJobsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListJobs",
      requestType = com.aps.grpc.proto.ListJobsRequest.class,
      responseType = com.aps.grpc.proto.ListJobsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.aps.grpc.proto.ListJobsRequest,
      com.aps.grpc.proto.ListJobsResponse> getListJobsMethod() {
    io.grpc.MethodDescriptor<com.aps.grpc.proto.ListJobsRequest, com.aps.grpc.proto.ListJobsResponse> getListJobsMethod;
    if ((getListJobsMethod = ApsServiceGrpc.getListJobsMethod) == null) {
      synchronized (ApsServiceGrpc.class) {
        if ((getListJobsMethod = ApsServiceGrpc.getListJobsMethod) == null) {
          ApsServiceGrpc.getListJobsMethod = getListJobsMethod =
              io.grpc.MethodDescriptor.<com.aps.grpc.proto.ListJobsRequest, com.aps.grpc.proto.ListJobsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListJobs"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.aps.grpc.proto.ListJobsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.aps.grpc.proto.ListJobsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ApsServiceMethodDescriptorSupplier("ListJobs"))
              .build();
        }
      }
    }
    return getListJobsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ApsServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ApsServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ApsServiceStub>() {
        @java.lang.Override
        public ApsServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ApsServiceStub(channel, callOptions);
        }
      };
    return ApsServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ApsServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ApsServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ApsServiceBlockingStub>() {
        @java.lang.Override
        public ApsServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ApsServiceBlockingStub(channel, callOptions);
        }
      };
    return ApsServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ApsServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ApsServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ApsServiceFutureStub>() {
        @java.lang.Override
        public ApsServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ApsServiceFutureStub(channel, callOptions);
        }
      };
    return ApsServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     * <pre>
     * 同步求解（阻塞等待结果）
     * </pre>
     */
    default void solve(com.aps.grpc.proto.SolveRequest request,
        io.grpc.stub.StreamObserver<com.aps.grpc.proto.SolveResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSolveMethod(), responseObserver);
    }

    /**
     * <pre>
     * 异步任务提交
     * </pre>
     */
    default void submitJob(com.aps.grpc.proto.SubmitJobRequest request,
        io.grpc.stub.StreamObserver<com.aps.grpc.proto.SubmitJobResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSubmitJobMethod(), responseObserver);
    }

    /**
     * <pre>
     * 查询任务状态
     * </pre>
     */
    default void getJobStatus(com.aps.grpc.proto.GetJobStatusRequest request,
        io.grpc.stub.StreamObserver<com.aps.grpc.proto.GetJobStatusResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetJobStatusMethod(), responseObserver);
    }

    /**
     * <pre>
     * 列出所有任务
     * </pre>
     */
    default void listJobs(com.aps.grpc.proto.ListJobsRequest request,
        io.grpc.stub.StreamObserver<com.aps.grpc.proto.ListJobsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListJobsMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service ApsService.
   */
  public static abstract class ApsServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return ApsServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service ApsService.
   */
  public static final class ApsServiceStub
      extends io.grpc.stub.AbstractAsyncStub<ApsServiceStub> {
    private ApsServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ApsServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ApsServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * 同步求解（阻塞等待结果）
     * </pre>
     */
    public void solve(com.aps.grpc.proto.SolveRequest request,
        io.grpc.stub.StreamObserver<com.aps.grpc.proto.SolveResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSolveMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * 异步任务提交
     * </pre>
     */
    public void submitJob(com.aps.grpc.proto.SubmitJobRequest request,
        io.grpc.stub.StreamObserver<com.aps.grpc.proto.SubmitJobResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSubmitJobMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * 查询任务状态
     * </pre>
     */
    public void getJobStatus(com.aps.grpc.proto.GetJobStatusRequest request,
        io.grpc.stub.StreamObserver<com.aps.grpc.proto.GetJobStatusResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetJobStatusMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * 列出所有任务
     * </pre>
     */
    public void listJobs(com.aps.grpc.proto.ListJobsRequest request,
        io.grpc.stub.StreamObserver<com.aps.grpc.proto.ListJobsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListJobsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service ApsService.
   */
  public static final class ApsServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<ApsServiceBlockingStub> {
    private ApsServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ApsServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ApsServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * 同步求解（阻塞等待结果）
     * </pre>
     */
    public com.aps.grpc.proto.SolveResponse solve(com.aps.grpc.proto.SolveRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSolveMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * 异步任务提交
     * </pre>
     */
    public com.aps.grpc.proto.SubmitJobResponse submitJob(com.aps.grpc.proto.SubmitJobRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSubmitJobMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * 查询任务状态
     * </pre>
     */
    public com.aps.grpc.proto.GetJobStatusResponse getJobStatus(com.aps.grpc.proto.GetJobStatusRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetJobStatusMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * 列出所有任务
     * </pre>
     */
    public com.aps.grpc.proto.ListJobsResponse listJobs(com.aps.grpc.proto.ListJobsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListJobsMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service ApsService.
   */
  public static final class ApsServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<ApsServiceFutureStub> {
    private ApsServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ApsServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ApsServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * 同步求解（阻塞等待结果）
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.aps.grpc.proto.SolveResponse> solve(
        com.aps.grpc.proto.SolveRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSolveMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * 异步任务提交
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.aps.grpc.proto.SubmitJobResponse> submitJob(
        com.aps.grpc.proto.SubmitJobRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSubmitJobMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * 查询任务状态
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.aps.grpc.proto.GetJobStatusResponse> getJobStatus(
        com.aps.grpc.proto.GetJobStatusRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetJobStatusMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * 列出所有任务
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.aps.grpc.proto.ListJobsResponse> listJobs(
        com.aps.grpc.proto.ListJobsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListJobsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SOLVE = 0;
  private static final int METHODID_SUBMIT_JOB = 1;
  private static final int METHODID_GET_JOB_STATUS = 2;
  private static final int METHODID_LIST_JOBS = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SOLVE:
          serviceImpl.solve((com.aps.grpc.proto.SolveRequest) request,
              (io.grpc.stub.StreamObserver<com.aps.grpc.proto.SolveResponse>) responseObserver);
          break;
        case METHODID_SUBMIT_JOB:
          serviceImpl.submitJob((com.aps.grpc.proto.SubmitJobRequest) request,
              (io.grpc.stub.StreamObserver<com.aps.grpc.proto.SubmitJobResponse>) responseObserver);
          break;
        case METHODID_GET_JOB_STATUS:
          serviceImpl.getJobStatus((com.aps.grpc.proto.GetJobStatusRequest) request,
              (io.grpc.stub.StreamObserver<com.aps.grpc.proto.GetJobStatusResponse>) responseObserver);
          break;
        case METHODID_LIST_JOBS:
          serviceImpl.listJobs((com.aps.grpc.proto.ListJobsRequest) request,
              (io.grpc.stub.StreamObserver<com.aps.grpc.proto.ListJobsResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getSolveMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.aps.grpc.proto.SolveRequest,
              com.aps.grpc.proto.SolveResponse>(
                service, METHODID_SOLVE)))
        .addMethod(
          getSubmitJobMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.aps.grpc.proto.SubmitJobRequest,
              com.aps.grpc.proto.SubmitJobResponse>(
                service, METHODID_SUBMIT_JOB)))
        .addMethod(
          getGetJobStatusMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.aps.grpc.proto.GetJobStatusRequest,
              com.aps.grpc.proto.GetJobStatusResponse>(
                service, METHODID_GET_JOB_STATUS)))
        .addMethod(
          getListJobsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.aps.grpc.proto.ListJobsRequest,
              com.aps.grpc.proto.ListJobsResponse>(
                service, METHODID_LIST_JOBS)))
        .build();
  }

  private static abstract class ApsServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ApsServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.aps.grpc.proto.ApsProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ApsService");
    }
  }

  private static final class ApsServiceFileDescriptorSupplier
      extends ApsServiceBaseDescriptorSupplier {
    ApsServiceFileDescriptorSupplier() {}
  }

  private static final class ApsServiceMethodDescriptorSupplier
      extends ApsServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    ApsServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ApsServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ApsServiceFileDescriptorSupplier())
              .addMethod(getSolveMethod())
              .addMethod(getSubmitJobMethod())
              .addMethod(getGetJobStatusMethod())
              .addMethod(getListJobsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
