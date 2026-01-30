use std::sync::atomic::{AtomicU64, Ordering};
use std::time::{SystemTime, UNIX_EPOCH};
use serde::{Deserialize, Serialize};

/// 系统指标
#[derive(Debug, Default, Serialize, Deserialize)]
pub struct SystemMetrics {
    pub total_requests: u64,
    pub successful_requests: u64,
    pub failed_requests: u64,
    pub average_response_time_ms: f64,
    pub active_jobs: u64,
    pub memory_usage_mb: f64,
    pub cpu_usage_percent: f64,
    pub uptime_seconds: u64,
}

/// 性能指标收集器
#[derive(Debug, Default)]
pub struct MetricsCollector {
    total_requests: AtomicU64,
    successful_requests: AtomicU64,
    failed_requests: AtomicU64,
    total_response_time_ms: AtomicU64,
}

impl MetricsCollector {
    pub fn new() -> Self {
        Self::default()
    }

    pub fn record_request(&self, success: bool, response_time_ms: u64) {
        self.total_requests.fetch_add(1, Ordering::Relaxed);
        if success {
            self.successful_requests.fetch_add(1, Ordering::Relaxed);
        } else {
            self.failed_requests.fetch_add(1, Ordering::Relaxed);
        }
        self.total_response_time_ms.fetch_add(response_time_ms, Ordering::Relaxed);
    }

    pub fn get_metrics(&self) -> SystemMetrics {
        let total = self.total_requests.load(Ordering::Relaxed);
        let success = self.successful_requests.load(Ordering::Relaxed);
        let failed = self.failed_requests.load(Ordering::Relaxed);
        let total_time = self.total_response_time_ms.load(Ordering::Relaxed);
        
        let avg_time = if total > 0 {
            total_time as f64 / total as f64
        } else {
            0.0
        };

        SystemMetrics {
            total_requests: total,
            successful_requests: success,
            failed_requests: failed,
            average_response_time_ms: avg_time,
            active_jobs: 0, // Will be updated by job manager
            memory_usage_mb: 0.0, // Will be updated by monitoring
            cpu_usage_percent: 0.0, // Will be updated by monitoring
            uptime_seconds: SystemTime::now()
                .duration_since(UNIX_EPOCH)
                .unwrap()
                .as_secs(),
        }
    }

    pub fn reset(&self) {
        self.total_requests.store(0, Ordering::Relaxed);
        self.successful_requests.store(0, Ordering::Relaxed);
        self.failed_requests.store(0, Ordering::Relaxed);
        self.total_response_time_ms.store(0, Ordering::Relaxed);
    }
}

/// 健康检查响应
#[derive(Debug, Serialize, Deserialize)]
pub struct HealthResponse {
    pub status: String,
    pub timestamp: u64,
    pub version: String,
    pub uptime_seconds: u64,
    pub metrics: SystemMetrics,
}

impl HealthResponse {
    pub fn new(metrics: SystemMetrics) -> Self {
        Self {
            status: "healthy".to_string(),
            timestamp: SystemTime::now()
                .duration_since(UNIX_EPOCH)
                .unwrap()
                .as_secs(),
            version: env!("CARGO_PKG_VERSION").to_string(),
            uptime_seconds: metrics.uptime_seconds,
            metrics,
        }
    }
}
