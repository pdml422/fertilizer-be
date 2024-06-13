import sys
import os
import spectral as sp


def getResolution(path):
    # Set the SPECTRAL_DATA environment variable to the directory containing the data file
    data_dir = os.path.dirname(path)
    os.environ['SPECTRAL_DATA'] = data_dir

    # Load the image using Spectral
    img = sp.open_image(path)

    shape = img.shape
    width = int(shape[1])
    height = int(shape[0])

    print(f'{width} {height}')


if __name__ == '__main__':
    path = sys.argv[1]
    getResolution(path)
