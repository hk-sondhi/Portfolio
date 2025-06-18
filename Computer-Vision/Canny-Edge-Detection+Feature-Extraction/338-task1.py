import cv2  #For image processing tasks
import numpy as np  #For numerical operations
import matplotlib.pyplot as plt #For displaying images and visualising data
import time

# Start timer
start_time = time.time()


#Reads image
image_path = '/Users/SimiSondhi/Downloads/flower.jpg'  
image = cv2.imread(image_path)

#Convert to greyscale
gray_image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

#Display the greyscale image
plt.imshow(gray_image, cmap='gray')
plt.title('Grayscale Image')
plt.axis('off')
plt.show()

#Applying Gaussian Blur
img = image.copy()  #Original image for display
blur = cv2.GaussianBlur(img,(3,3),0)    #Gaussian blur with 3x3 kernal

#Dispalying original and blurred lines
plt.subplot(121),plt.imshow(img),plt.title('Original')
plt.xticks([]), plt.yticks([])
plt.subplot(122),plt.imshow(blur),plt.title('Gaussian Blur')
plt.xticks([]), plt.yticks([])
plt.show()

img = gray_image.copy()

#Computing gradients using lapalacian and Sobel
laplacian = cv2.Laplacian(img,cv2.CV_64F)   #Detect edges by calculating the second derivatives of the image
sobelx = cv2.Sobel(img,cv2.CV_64F,1,0,ksize=5)  #Detects edges in the horizontal direction by computing the gradient along the x-axis.
sobely = cv2.Sobel(img,cv2.CV_64F,0,1,ksize=5)  #Detects edges in the vertical direction by computing the gradient along the y-axis.

plt.subplot(2,2,1),plt.imshow(img,cmap = 'gray')    #Displays the original grayscale image 
plt.title('Original'), plt.xticks([]), plt.yticks([])
plt.subplot(2,2,2),plt.imshow(laplacian,cmap = 'gray')   #Displays the Laplacian edge-detected image
plt.title('Laplacian'), plt.xticks([]), plt.yticks([]) 
plt.subplot(2,2,3),plt.imshow(sobelx,cmap = 'gray') #Displays the horizontal edge-detected image (Sobel X)
plt.title('Sobel X'), plt.xticks([]), plt.yticks([])    
plt.subplot(2,2,4),plt.imshow(sobely,cmap = 'gray') #Displays the vertical edge-detected image (Sobel Y)
plt.title('Sobel Y'), plt.xticks([]), plt.yticks([])
plt.show()

# Compute gradient magnitude and direction
gradient_magnitude = np.sqrt(sobelx**2 + sobely**2)
gradient_direction = np.arctan2(sobely, sobelx) * (180 / np.pi)  # Converts to degrees
gradient_direction = (gradient_direction + 180) % 180  # ensure graient direction is within range [0, 180] degrees

# Non-Maximum Suppression function
def non_maximum_suppression(magnitude, direction):
    X, Y = magnitude.shape  #where x and y are dimensions of the image 
    suppressed = np.zeros((X, Y), dtype=np.float32) #matrix to store output after NMS
    
    for i in range(1, X-1): #loop to go through each pixel in image
        for j in range(1, Y-1): 
            angle = direction[i, j]%100 #gradient direction is normalised to range [0, 180] and % ensures consistency even if angle exceeds 180 degrees
            
            # Determine neighbors based on gradient direction
            if (0 <= angle < 22.5) or (157.5 <= angle <= 180):  #horizontal direction
                pixels = [magnitude[i, j+1], magnitude[i, j-1]]
            elif 22.5 <= angle < 67.5:  #diagnoal direction
                pixels = [magnitude[i+1, j-1], magnitude[i-1, j+1]]
            elif 67.5 <= angle < 112.5: #vertical direction
                pixels = [magnitude[i+1, j], magnitude[i-1, j]]
            elif 112.5 <= angle < 157.5:    #other direction
                pixels = [magnitude[i-1, j-1], magnitude[i+1, j+1]]
            
            # suppression: if magnitude is greater or equal to its neighbouring pixels in the gradient direction, it is a local maximum and kept 
            # else, pixel is set to 0
            if magnitude[i, j] >= max(pixels): 
                suppressed[i, j] = magnitude[i, j]
    
    return suppressed

# Apply Non-Maximum Suppression
nms_result = non_maximum_suppression(gradient_magnitude, gradient_direction)

# Display the NMS result
plt.imshow(nms_result, cmap='gray')
plt.title('Non-Maximum Suppression')
plt.axis('off')
plt.show()

# Set thresholds for double thresholding
low_threshold = 550
high_threshold = 1000

def double_threshold(image, low_threshold, high_threshold):
    strong = 255  # Value for strong edges
    weak = 100    # Value for weak edges
    
    strong_edges = (image >= high_threshold)    #boolean array where True corresponds to pixels in the image with values greater or equal to strong edges
    weak_edges = (image >= low_threshold) & (image < high_threshold)    #boolean array where True corresponds with values between low and high threshold (weak edges)
    
    # Create an output image
    thresholded = np.zeros_like(image, dtype=np.uint8)  #creates image with the same shape as input and data type of unit8
    thresholded[strong_edges] = strong #sets pixels in threshold to 255 (strong edge value)
    thresholded[weak_edges] = weak #sets pixels in threshold to 100 (strong edge value)

    #All other pixels remain at 0, for the non-edges
    return thresholded, weak, strong

# Apply Double Thresholding
thresholded_result, weak, strong = double_threshold(nms_result, low_threshold, high_threshold)

# Display the result of Double Thresholding
plt.imshow(thresholded_result, cmap='gray')
plt.title('Double Thresholding')
plt.axis('off')
plt.show()


def edge_tracking_by_hysteresis(thresholded, weak, strong):
    rows, cols = thresholded.shape  #extracts the number of rows and columns in the threshold image
    for i in range(1, rows - 1):    #loop that goes through each pixel in image 
        for j in range(1, cols - 1):
            if thresholded[i, j] == weak:   #to focus on pixels with weak edges in threshold image
                # Check 8-connected neighbouring pixels for strong edges
                if ((thresholded[i+1, j-1] == strong) or (thresholded[i+1, j] == strong) or 
                    (thresholded[i+1, j+1] == strong) or (thresholded[i, j-1] == strong) or 
                    (thresholded[i, j+1] == strong) or (thresholded[i-1, j-1] == strong) or 
                    (thresholded[i-1, j] == strong) or (thresholded[i-1, j+1] == strong)):
                    thresholded[i, j] = strong  #if a weak edge is connected to a strong edge, becomes strong edge
                else:
                    thresholded[i, j] = 0  #if weak edge pixel has no strong edge neighbouring pixels, its suppressed so marked as non-edge
    return thresholded

# Apply Edge Tracking by Hysteresis
final_edges = edge_tracking_by_hysteresis(thresholded_result, weak, strong)

# Display the final edge-detected image
plt.imshow(final_edges, cmap='gray')
plt.title('Final Edges after Hysteresis')
plt.axis('off')
plt.show()

# End timer
end_time = time.time()

# Calculate and print running time
print(f"Execution time: {end_time - start_time:.4f} seconds")





