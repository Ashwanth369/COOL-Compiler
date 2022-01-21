# Sample Python Analytic for Alerts

Analytic to calculate sum, average and standard deviation. The analytic also compares each input value against a threshold and generates alerts.

## Analytic Input
  * `time_stamp`: Any date-time format input. 
  * `inputs`: Can have n number of inputs for a time stamp.

## Analytic Output
  * `daily_fired_hours`: Maximum fired hours observed in the last 24 hours
  * `daily_avg`: Average value of avg_tag after removing outliers
  * `Note`: Analytic needs atleast 12 datapoints of good data (or 1 hour)
  * `Note`: No output will be written if the unit is not operating


## Input format
Here is a sample input json file:

```json
{
    "data" : {
        "time_series" : {
            "time_stamp" : [
                1,
                2,
                3,
                4
            ],
            "input1" : [
                8,
                2,
                4,
                6
            ],
            "input2" : [
                3,
                4,
                1,
                7
            ],
            "input3" : [
                9,
                1,
                5,
                3
            ]
        },
        "constants" : {
            "constant1" : 2,
            "constant2" : 3,
            "threshold" : 4
        }
    }
}
```

## Output format
The output for the above sample input analytic would be:

```json
{
    "time_series": {
        "time_stamp": [
            1,
            2,
            3,
            4
        ],
        "sum": [
            10.0,
            3.5,
            5.0,
            8.0
        ],
        "mean": [
            6.667,
            2.333,
            3.333,
            5.333
        ],
        "deviation": [
            1,
            0,
            0,
            1
        ]
    },
    "alerts": {
        "time_stamp": [
            1,
            4
        ],
        "score": [
            9,
            7
        ],
        "sensor": [
            3,
            2
        ]
    }
}
```

