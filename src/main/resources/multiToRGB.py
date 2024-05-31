import sys
import cv2
import rasterio
import numpy as np


def multiToRGB(id, tif_path, red, green, blue):
    # Specify the path to your TIF file
    file_path = tif_path

    # Open the TIF file
    with rasterio.open(file_path) as src:
        # Read the image data
        image = src.read()

    red_band = image[red]
    green_band = image[green]
    blue_band = image[blue]

    rgb_image = np.dstack((blue_band, green_band, red_band))
    rgb_image *= 10
    cv2.imwrite(f'src/main/resources/Image/{userId}/Output/multi_{id}_{red}_{green}_{blue}.png', rgb_image)

    print(f'http://localhost:8888/src/main/resources/Image/{userId}/Output/multi_{id}_{red}_{green}_{blue}.png')


if __name__ == '__main__':
    id = int(sys.argv[1])
    userId = int(sys.argv[2])
    tif_path = sys.argv[3]
    red = int(sys.argv[4])
    green = int(sys.argv[5])
    blue = int(sys.argv[6])
    multiToRGB(id, tif_path, red, green, blue)
