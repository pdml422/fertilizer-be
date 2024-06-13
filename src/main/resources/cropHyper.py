import os
import sys
import cv2
import spectral as sp
import numpy as np


def crop_center(img, x, y, cropx, cropy):
    return cv2.getRectSubPix(img, (cropx, cropy), (x, y))


def hyperToRGB(id, hdr_path, x, y):
    # Set the SPECTRAL_DATA environment variable to the directory containing the data file
    data_dir = os.path.dirname(hdr_path)
    os.environ['SPECTRAL_DATA'] = data_dir

    # Load the image using Spectral
    img = sp.open_image(hdr_path)

    # Specify band indices for red, green, and blue channels
    red_band_index = 40
    green_band_index = 30
    blue_band_index = 20

    # Read individual bands from the image using the specified band indices
    red_band = img.read_band(red_band_index)
    green_band = img.read_band(green_band_index)
    blue_band = img.read_band(blue_band_index)

    # Stack the individual bands to create an RGB image
    rgb_image = np.dstack((blue_band, green_band, red_band))
    rgb_image = rgb_image * 7

    # Crop the image at x, y
    cropped_image = crop_center(rgb_image, x, y, 144, 144)

    cv2.imwrite(f'src/main/resources/Image/{userId}/Output/hyper_crop_{id}_{x}_{y}.png', cropped_image)

    print(f'http://localhost:8888/src/main/resources/Image/{userId}/Output/hyper_crop_{id}_{x}_{y}.png')


if __name__ == '__main__':
    id = int(sys.argv[1])
    userId = int(sys.argv[2])
    hdr_path = sys.argv[3]
    x = int(sys.argv[4])
    y = int(sys.argv[5])
    hyperToRGB(id, hdr_path, x, y)
