import numpy as np
import cv2 as cv
from matplotlib import pyplot as plt
 
img1 = cv.imread('/Users/SimiSondhi/Downloads/Images for task2 (1) 2/victoria1.jpg', cv.IMREAD_GRAYSCALE)
img2 = cv.imread('/Users/SimiSondhi/Downloads/Images for task2 (1) 2/victoria2.jpg', cv.IMREAD_GRAYSCALE)
 
# Initiate ORB detector
orb = cv.ORB_create()
 
# find the keypoints with ORB
kp1 = orb.detect(img1,None)
kp2 = orb.detect(img2,None)
 
# compute the descriptors with ORB
kp1, des1 = orb.compute(img1, kp1)
kp2, des2 = orb.compute(img2, kp2)
 
# draw only keypoints location,not size and orientation
img1_kp = cv.drawKeypoints(img1, kp1, None, color=(0,255,0), flags=0)
img2_kp = cv.drawKeypoints(img2, kp2, None, color=(0,255,0), flags=0)

plt.subplot(1, 2, 1)
plt.title('Keypoints in victoria1.jpg')
plt.imshow(img1_kp, cmap='gray')
plt.axis('off')

plt.subplot(1, 2, 2)
plt.title('Keypoints in victoria2.jpg')
plt.imshow(img2_kp, cmap='gray')
plt.axis('off')

plt.show()


