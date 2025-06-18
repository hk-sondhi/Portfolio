# TripleMA-Crossover-R
# University - 2nd year

## Overview
This project implements a quantitative trading strategy in R using a triple moving average crossover approach. It analyzes price data to generate buy/sell signals based on short, medium, and long-term trend patterns.

## Features
- Signal generation based on SMA crossovers
- Dynamic position sizing based on current price
- Validation checks for input integrity
- Designed to work with multi-asset time series data

## Technologies Used
- R
- `TTR` package (for SMA calculation)
- `xts`
- Custom-built strategy logic

## How to Run
1. Prepare input time series data in the expected format.
2. Use the `getOrders()` function for signal generation at each timestep.
3. Use the output `marketOrders` to simulate or execute trades.

## Author
hk-sondhi

