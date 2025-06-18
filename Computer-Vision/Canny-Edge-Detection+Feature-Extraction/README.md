# Canny Edge Detection + Feature Extraction
# computer vision - 3rd year - university


## Overview
This project implements a full pipeline for image processing and computer vision, combining a **custom implementation of the Canny Edge Detection algorithm** with **feature extraction and comparison using SIFT and ORB**. It highlights both low-level image analysis and high-level feature detection and matching techniques.

## Tasks
### 🔹 Task 1: Canny Edge Detection
A step-by-step manual implementation of the Canny edge detection process, including:
- Grayscale conversion
- Gaussian blur for noise reduction
- Gradient calculation using Sobel filters
- Non-Maximum Suppression
- Double Thresholding
- Edge Tracking by Hysteresis

The implementation is benchmarked against OpenCV’s built-in `cv2.Canny()` function, and the performance trade-offs (e.g., accuracy vs. speed) are discussed.

### 🔹 Task 2: Feature Extraction and Matching
#### 2.1 — Research:
- Compared **SIFT**, **SURF**, and **ORB** in terms of scale/rotation invariance, speed, and robustness.

#### 2.2 — Feature Extraction:
- Applied feature detection using SIFT and ORB via OpenCV.
- Extracted and visualized keypoints.

#### 2.3 — Feature Matching:
- Compared SIFT and ORB based on accuracy.
- Demonstrated how ORB’s efficiency comes at the cost of precision, while SIFT achieved more reliable matches.

## Technologies Used
- Python
- OpenCV
- NumPy
- Matplotlib 


