pub mod alg;
pub mod async_job;
pub mod eval;
pub mod gen;
pub mod model;
pub mod observability;
pub mod service;
pub mod util;

// ✅ 导出常用类型，方便外部使用
pub use model::{
    ProcessType,
    Job,
    SolveInput,
    SolveOutput,
    SolveParams,
    Weights,
    Limits,
    KpiSummary,
    ScheduleItem,
};

pub use alg::{
    baseline::solve_edd,
    sa::solve_sa,
    hybrid::solve_hybrid,
};

// ✅ 添加统一求解入口
/// 统一求解入口函数
///
/// 根据输入参数中的算法类型自动分发到对应的求解器
///
/// # 支持的算法
/// - `"baseline"` - EDD (Earliest Due Date) 基准算法
/// - `"sa"` - Simulated Annealing 模拟退火
/// - `"hybrid"` - Multi-Start SA 多起点模拟退火
///
/// # Example
/// ```no_run
/// use aps_engine::{solve, SolveInput, SolveParams};
///
/// let input = SolveInput {
///     request_id: "test-001".to_string(),
///     plan_start_epoch_ms: 1704106800000,
///     jobs: vec![],
///     params: SolveParams {
///         algorithm: "sa".to_string(),
///         time_budget_sec: 5,
///         ..Default::default()
///     },
/// };
///
/// let output = solve(&input);
/// println!("成本: {}", output.summary.cost);
/// ```
pub fn solve(input: &SolveInput) -> SolveOutput {
    let algorithm = input.params.algorithm.as_str();

    match algorithm {
        "baseline" => solve_edd(input),
        "sa" => solve_sa(input),
        "hybrid" => solve_hybrid(input),
        _ => {
            eprintln!("⚠️  Unknown algorithm: '{}', falling back to baseline", algorithm);
            solve_edd(input)
        }
    }
}
