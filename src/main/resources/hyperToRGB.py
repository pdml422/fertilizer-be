import os
import sys
import cv2
import spectral as sp
import numpy as np


def hyperToRGB(id, hdr_path, red, green, blue):
    # Set the SPECTRAL_DATA environment variable to the directory containing the data file
    data_dir = os.path.dirname(hdr_path)
    os.environ['SPECTRAL_DATA'] = data_dir

    # Load the image using Spectral
    img = sp.open_image(hdr_path)

    # Specify band indices for red, green, and blue channels
    red_band_index = red
    green_band_index = green
    blue_band_index = blue

    # Read individual bands from the image using the specified band indices
    red_band = img.read_band(red_band_index)
    green_band = img.read_band(green_band_index)
    blue_band = img.read_band(blue_band_index)

    # Stack the individual bands to create an RGB image
    rgb_image = np.dstack((blue_band, green_band, red_band))
    rgb_image = rgb_image * 7

    cv2.imwrite(f'src/main/resources/Image/{userId}/Output/hyper_{id}_{red_band_index}_{green_band_index}_{blue_band_index}.png', rgb_image)

    print(f'http://localhost:8888/src/main/resources/Image/{userId}/Output/hyper_{id}_{red_band_index}_{green_band_index}_{blue_band_index}.png')


if __name__ == '__main__':
    id = int(sys.argv[1])
    userId = int(sys.argv[2])
    hdr_path = sys.argv[3]
    red = int(sys.argv[4])
    green = int(sys.argv[5])
    blue = int(sys.argv[6])
    hyperToRGB(id, hdr_path, red, green, blue)
