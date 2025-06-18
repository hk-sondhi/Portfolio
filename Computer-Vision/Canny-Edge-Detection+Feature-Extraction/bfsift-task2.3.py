import numpy as np
import cv2 as cv
import matplotlib.pyplot as plt
 
# Load images
img1 = cv.imread('/Users/SimiSondhi/Downloads/Images for task2 (1) 2/victoria1.jpg', cv.IMREAD_GRAYSCALE)
img2 = cv.imread('/Users/SimiSondhi/Downloads/Images for task2 (1) 2/victoria2.jpg', cv.IMREAD_GRAYSCALE)


# Initiate SIFT detector
sift = cv.SIFT_create()
 
# find the keypoints and descriptors with SIFT
kp1, des1 = sift.detectAndCompute(img1,None)
kp2, des2 = sift.detectAndCompute(img2,None)
 
# BFMatcher with default params
bf = cv.BFMatcher()
matches = bf.knnMatch(des1,des2,k=2)
 
# Apply ratio test
good = []
for m,n in matches:
    if m.distance < 0.79*n.distance:
        good.append([m])
 
# cv.drawMatchesKnn expects list of lists as matches.
img3 = cv.drawMatchesKnn(img1,kp1,img2,kp2,good,None,flags=cv.DrawMatchesFlags_NOT_DRAW_SINGLE_POINTS)

plt.imshow(img3),plt.show()


