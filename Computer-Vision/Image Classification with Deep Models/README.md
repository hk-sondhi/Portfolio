# Image Classification with Deep Models (CIFAR-100)
# computer vision module - 3rd year - university

## Overview
This project applies deep learning techniques to the CIFAR-100 dataset using two models: a baseline CNN and an enhanced version with optimizations. The goal is to improve classification accuracy through architectural and training improvements such as Batch Normalization, Dropout, and Learning Rate Scheduling.

## Baseline Model
A lightweight Convolutional Neural Network (CNN) with three convolutional layers followed by dense layers. Chosen for its simplicity and fast convergence, the model balances efficiency with performance.

- **Architecture**:
  - 3 Conv layers (32, 64, 128 filters)
  - MaxPooling after each Conv
  - Dense layer with 256 neurons + Dropout
  - Output: Softmax with 100 classes

- **Training Results**:
  - Accuracy: **44.66%**
  - Loss: 2.1194

- **Limitations**:
  - Overfitting
  - Slow convergence
  - Lack of Batch Normalization or LR Scheduling

## Improved Model
A deeper CNN with:
- Batch Normalization (BN) after each Conv layer
- Increased Conv layers in 3 blocks
- Learning Rate Scheduling (ReduceLROnPlateau)
- Early Stopping for regularization
- Higher Dropout rates to reduce overfitting

- **Training Results**:
  - Accuracy: **61.23%**
  - Loss: 1.3644

- **Key Improvements**:
  - 16.57% increase in test accuracy
  - Faster, more stable training
  - Better generalization on unseen data

## Technologies Used
- Python
- TensorFlow / Keras
- CIFAR-100 Dataset
- NumPy, Matplotlib (for analysis/visualization)

