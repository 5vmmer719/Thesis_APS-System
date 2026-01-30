use chrono::{DateTime, Utc, Timelike};

/// 计算给定时间戳对应的班次ID
pub fn shift_id(epoch_ms: i64) -> String {
    let datetime = DateTime::<Utc>::from_timestamp_millis(epoch_ms)
        .unwrap_or_else(|| DateTime::<Utc>::from_timestamp(0, 0).unwrap());
    
    let hour = datetime.hour();
    let date_str = datetime.format("%Y-%m-%d").to_string();
    
    if hour >= 8 && hour < 20 {
        format!("{}_DAY", date_str)
    } else {
        let effective_date = if hour < 8 {
            datetime.date_naive() - chrono::Duration::days(1)
        } else {
            datetime.date_naive()
        };
        format!("{}_NIGHT", effective_date.format("%Y-%m-%d"))
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use chrono::{NaiveDate, NaiveTime};

    fn create_datetime(year: i32, month: u32, day: u32, hour: u32, min: u32) -> i64 {
        let date = NaiveDate::from_ymd_opt(year, month, day).unwrap();
        let time = NaiveTime::from_hms_opt(hour, min, 0).unwrap();
        let datetime = date.and_time(time).and_utc();
        datetime.timestamp_millis()
    }

    #[test]
    fn test_boundary_times() {
        let base_date = 2024;
        let base_month = 1;
        let base_day = 15;

        let t0759 = create_datetime(base_date, base_month, base_day + 1, 7, 59);
        assert_eq!(shift_id(t0759), "2024-01-15_NIGHT");

        let t0800 = create_datetime(base_date, base_month, base_day, 8, 0);
        assert_eq!(shift_id(t0800), "2024-01-15_DAY");

        let t1959 = create_datetime(base_date, base_month, base_day, 19, 59);
        assert_eq!(shift_id(t1959), "2024-01-15_DAY");

        let t2000 = create_datetime(base_date, base_month, base_day, 20, 0);
        assert_eq!(shift_id(t2000), "2024-01-15_NIGHT");

        let t2300 = create_datetime(base_date, base_month, base_day, 23, 0);
        assert_eq!(shift_id(t2300), "2024-01-15_NIGHT");

        let t0200 = create_datetime(base_date, base_month, base_day + 1, 2, 0);
        assert_eq!(shift_id(t0200), "2024-01-15_NIGHT");
    }
}
