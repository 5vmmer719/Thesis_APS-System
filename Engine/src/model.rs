use serde::{Deserialize, Serialize};

// ========== 工艺类型枚举 ==========
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[repr(i32)]
pub enum ProcessType {
    Stamping = 1,  // 冲压
    Welding = 2,   // 焊装
    Painting = 3,  // 涂装
    Assembly = 4,  // 总装
}

impl ProcessType {
    pub fn all() -> [ProcessType; 4] {
        [Self::Stamping, Self::Welding, Self::Painting, Self::Assembly]
    }

    pub fn as_i32(&self) -> i32 {
        *self as i32
    }

    pub fn from_i32(value: i32) -> Option<Self> {
        match value {
            1 => Some(Self::Stamping),
            2 => Some(Self::Welding),
            3 => Some(Self::Painting),
            4 => Some(Self::Assembly),
            _ => None,
        }
    }

    pub fn name(&self) -> &'static str {
        match self {
            Self::Stamping => "冲压",
            Self::Welding => "焊装",
            Self::Painting => "涂装",
            Self::Assembly => "总装",
        }
    }
}

// ========== 作业定义 ==========
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Job {
    pub vin: String,
    pub due_epoch_ms: i64,

    // 四大工艺的加工时间（分钟）
    #[serde(default)]
    pub stamping_minutes: i32,
    #[serde(default)]
    pub welding_minutes: i32,
    #[serde(default)]
    pub painting_minutes: i32,
    pub assemble_minutes: i32,

    // 工艺属性（用于换型成本）
    #[serde(default)]
    pub mold_code: String,
    #[serde(default)]
    pub welding_fixture: String,
    pub color: String,
    pub config: String,

    pub energy_score: f64,
    pub emission_score: f64,
}

impl Job {
    /// 获取指定工艺的加工时间
    pub fn get_process_minutes(&self, process: ProcessType) -> i32 {
        match process {
            ProcessType::Stamping => self.stamping_minutes,
            ProcessType::Welding => self.welding_minutes,
            ProcessType::Painting => self.painting_minutes,
            ProcessType::Assembly => self.assemble_minutes,
        }
    }

    /// 获取工艺的切换键（用于换型矩阵）
    pub fn get_setup_key(&self, process: ProcessType) -> &str {
        match process {
            ProcessType::Stamping => &self.mold_code,
            ProcessType::Welding => &self.welding_fixture,
            ProcessType::Painting => &self.color,
            ProcessType::Assembly => &self.config,
        }
    }

    /// 计算总加工时间
    pub fn total_minutes(&self) -> i32 {
        self.stamping_minutes + self.welding_minutes
            + self.painting_minutes + self.assemble_minutes
    }
}

// ========== 权重和限制 ==========
#[derive(Debug, Clone, Serialize, Deserialize, Default)]
pub struct Weights {
    pub tardiness: f64,
    pub color_change: f64,
    pub config_change: f64,
    pub energy_excess: f64,
    pub emission_excess: f64,
    pub material_shortage: f64,
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
pub struct Limits {
    pub max_energy_per_shift: f64,
    pub max_emission_per_shift: f64,
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
pub struct SolveParams {
    pub algorithm: String,
    pub time_budget_sec: i32,
    pub seed: i64,
    pub weights: Weights,
    pub limits: Limits,
}

// ========== 违规信息 ==========
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ShiftViolation {
    pub shift_id: String,
    pub vtype: String,
    pub excess: f64,
}

// ========== KPI 汇总 ==========
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct KpiSummary {
    pub cost: f64,
    pub total_tardiness_min: i64,
    pub max_tardiness_min: i64,
    pub color_changes: i32,
    pub config_changes: i32,
    pub energy_excess: f64,
    pub emission_excess: f64,
    pub material_shortage: f64,
    pub elapsed_ms: i64,
}

// ========== 调度项 ==========
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ScheduleItem {
    pub seq: i32,
    pub vin: String,

    // 工艺信息
    #[serde(default = "default_process_type")]
    pub process_type: i32,

    // 产线信息
    #[serde(default)]
    pub line_id: i64,

    pub start_epoch_ms: i64,
    pub end_epoch_ms: i64,
    pub due_epoch_ms: i64,
    pub tardiness_min: i64,
    pub color: String,
    pub config: String,
    pub shift_id: String,

    // 班次内顺序
    #[serde(default)]
    pub seq_in_shift: i32,
}

fn default_process_type() -> i32 {
    4 // 默认总装
}

// ========== 收敛点 ==========
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ConvergencePoint {
    pub t_ms: i64,
    pub best_cost: f64,
}

// ========== 输入输出 ==========
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SolveInput {
    pub request_id: String,
    pub plan_start_epoch_ms: i64,
    pub jobs: Vec<Job>,
    pub params: SolveParams,
}

#[derive(Debug, Clone)]
pub struct SolveOutput {
    pub request_id: String,
    pub summary: KpiSummary,
    pub baseline_summary: KpiSummary,
    pub order: Vec<String>,
    pub schedule: Vec<ScheduleItem>,           // 简化版（每车一条）
    pub detailed_schedule: Vec<ScheduleItem>,  // ✅ 新增：详细版（每工序一条）
    pub violations: Vec<ShiftViolation>,
    pub convergence: Vec<ConvergencePoint>,
}


// ========== 单元测试 ==========
#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_process_type() {
        assert_eq!(ProcessType::Stamping.as_i32(), 1);
        assert_eq!(ProcessType::Assembly.as_i32(), 4);
        assert_eq!(ProcessType::from_i32(3), Some(ProcessType::Painting));
        assert_eq!(ProcessType::Painting.name(), "涂装");
    }

    #[test]
    fn test_backward_compatibility() {
        let old_json = r#"{
            "vin": "VIN001",
            "due_epoch_ms": 1704110400000,
            "color": "RED",
            "config": "BASE",
            "assemble_minutes": 60,
            "energy_score": 100.0,
            "emission_score": 50.0
        }"#;

        let job: Job = serde_json::from_str(old_json).unwrap();

        assert_eq!(job.stamping_minutes, 0);
        assert_eq!(job.welding_minutes, 0);
        assert_eq!(job.painting_minutes, 0);
        assert_eq!(job.assemble_minutes, 60);
        assert_eq!(job.mold_code, "");
        assert_eq!(job.vin, "VIN001");
    }

    #[test]
    fn test_job_methods() {
        let job = Job {
            vin: "VIN002".to_string(),
            due_epoch_ms: 1704110400000,
            stamping_minutes: 10,
            welding_minutes: 20,
            painting_minutes: 30,
            assemble_minutes: 40,
            mold_code: "MOLD_A".to_string(),
            welding_fixture: "FIX_B".to_string(),
            color: "BLUE".to_string(),
            config: "PREMIUM".to_string(),
            energy_score: 80.0,
            emission_score: 40.0,
        };

        assert_eq!(job.get_process_minutes(ProcessType::Stamping), 10);
        assert_eq!(job.get_setup_key(ProcessType::Painting), "BLUE");
        assert_eq!(job.total_minutes(), 100);
    }

    #[test]
    fn test_schedule_item_defaults() {
        let old_json = r#"{
            "seq": 1,
            "vin": "VIN001",
            "start_epoch_ms": 1704106800000,
            "end_epoch_ms": 1704110400000,
            "due_epoch_ms": 1704110400000,
            "tardiness_min": 0,
            "color": "RED",
            "config": "BASE",
            "shift_id": "D1"
        }"#;

        let item: ScheduleItem = serde_json::from_str(old_json).unwrap();

        assert_eq!(item.process_type, 4);
        assert_eq!(item.line_id, 0);
        assert_eq!(item.seq_in_shift, 0);
    }
}
