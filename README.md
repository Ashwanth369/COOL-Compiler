This is a sample python analytic provided by Analytics(CAF) team. 
1. This analytic takes n no of inputs and generated 2 outputs mean and deviation. 
2. Input names can vary
3. Outputs have to be specific names.
4. One constant of threshold must be present. 
5. Analytic compares each input value against the threshold and creates alerts and output data. 
6. One alert is created for each value > threshold. 
7. This Analytics takes Time Series data as input
8. Generates Alerts and Time Series data as output

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

