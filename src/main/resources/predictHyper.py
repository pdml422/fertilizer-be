import os.path
import sys

import cv2
import numpy as np
import spectral as sp
from matplotlib import pyplot as plt
from sklearn.decomposition import PCA
from tensorflow.keras.models import load_model


def crop_and_stack_bands(img, crop_size=144, x=None, y=None):
    # Get the number of bands
    num_bands = img.shape[2]

    # Initialize an empty list to store the cropped bands
    cropped_bands_list = []

    # Loop through each band
    for band in range(num_bands):
        # Read the current band
        current_band = img.read_band(band)  # Bands in Spectral start from 1

        # Set default starting points if not provided
        if x is None:
            x = current_band.shape[0] // 2
        if y is None:
            y = current_band.shape[1] // 2

        # Calculate the cropping boundaries
        start_x = x - crop_size // 2
        start_y = y - crop_size // 2
        end_x = start_x + crop_size
        end_y = start_y + crop_size

        # Crop the band
        cropped_band = current_band[start_x:end_x, start_y:end_y]

        # Append the cropped band to the list
        cropped_bands_list.append(cropped_band)

    # Convert the list of cropped bands to a NumPy array
    cropped_bands = np.stack(cropped_bands_list, axis=-1)
    return cropped_bands


def preprocess_and_predict(model, hdr_path, x, y):
    # Load the hyperspectral image
    img = sp.open_image(hdr_path)

    # Crop and stack bands
    cropped_array = crop_and_stack_bands(img, x=x, y=y)

    # PCA
    pca = PCA(30)
    reshaped_array = cropped_array.reshape(-1, cropped_array.shape[2])
    pca_result = pca.fit_transform(reshaped_array)
    pca_result_reshaped = pca_result.reshape(cropped_array.shape[0], cropped_array.shape[1], 30)

    # Reshape the data to match the model input shape
    input_reshaped = pca_result_reshaped.reshape(1, 144, 144, 30, 1)

    # Make predictions
    predictions = model.predict(input_reshaped)

    # Return the predicted mask
    return predictions[0]


if __name__ == '__main__':
    hdr_path = sys.argv[1]
    model = sys.argv[2]
    x = int(sys.argv[3])
    y = int(sys.argv[4])
    userId = int(sys.argv[5])
    imageId = int(sys.argv[6])
    modelId = int(sys.argv[7])

    model = load_model(model)

    # Perform model prediction
    predicted_mask = preprocess_and_predict(model, hdr_path, x, y)
    plt.imsave(f'src/main/resources/Image/{userId}/Output/predict_hyper_{imageId}_{modelId}_{x}_{y}.png', np.argmax(predicted_mask, axis=-1))
    print(f'http://localhost:8888/src/main/resources/Image/{userId}/Output/predict_hyper_{imageId}_{modelId}_{x}_{y}.png')
