# Sample Python Analytic for Alerts

Analytic to calculate sum, average and standard deviation. The analytic also compares each input value against a threshold and generates alerts.

## Analytic Input

### Time Series
  * `time_stamp`: Any date-time format input. 
  * `inputs`: Can have n number of inputs for a time stamp.

### Constant
  * `threshold`: A threshold to compare for the input inorder to generate alerts.
  * `constant1`: Optional constant (default value 1).
  * `constant2`: Optional constant (default value 1). 

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
            "threshold" : 4,
            "constant1" : 2,
            "constant2" : 3
        }
    }
}
```

## Analytic Output

### Time Series
  * `time_stamp`: Same as the time_stamp in input. 
  * `sum`: Arithemetic sum of the inputs corresponding to each time stamp divided by the constant1.
  * `mean`: Arithemetic mean of the inputs corresponding to each time stamp.
  * `std`: Standard deviation of the inputs corresponding to each time stamp.
  * `deviation`: A boolean output that is 1 when arithemetic sum of the inputs corresponding to each time stamp divided by the constant2 is greater than threshold, else 0.

### Alerts
  * `time_stamp`: Time stamp of the inputs which have a deviation value of 1.
  * `score`: The maximum value among the inputs from the above time stamps.
  * `sensor`: The index of the score in their respective time stamps.

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
        "std": [
            3.215,
            1.528,
            2.082,
            2.082
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

