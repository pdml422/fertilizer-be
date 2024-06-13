import os
import sys
import os.path
import random
import math
import cv2

import matplotlib.pyplot as plt
import numpy as np
import tifffile as tiff
import imagecodecs
from PIL import Image

import tensorflow as tf
from tensorflow.keras.layers import BatchNormalization
from tensorflow.keras.models import Model
from tensorflow.keras.layers import (Input,
                                     Conv2D,
                                     MaxPooling2D,
                                     UpSampling2D,
                                     concatenate,
                                     Conv2DTranspose,
                                     Dropout)
from tensorflow.keras.optimizers import Adam
from tensorflow.keras.optimizers import RMSprop
from tensorflow.keras.optimizers import SGD
from tensorflow.keras.callbacks import (CSVLogger,
                                        ModelCheckpoint,
                                        EarlyStopping,
                                        ReduceLROnPlateau,
                                        TensorBoard)
from tensorflow.keras.metrics import MeanIoU

DEFAULT_PATCH_SIZE = 320


def get_patches(x_dict, y_dict, nb_patches, size=DEFAULT_PATCH_SIZE):
    x = []
    y = []
    total_patches = 0

    while total_patches < nb_patches:
        img_id = random.sample(x_dict.keys(), 1)[0]
        img = x_dict[img_id]
        mask = y_dict[img_id]
        img_patch, mask_patch = get_rand_patch(img, mask, size)
        x.append(img_patch)
        y.append(mask_patch)
        total_patches += 1

    print("Generated {} patches".format(total_patches))

    return np.array(x), np.array(y)


def unet_model(nb_classes=3, img_size=DEFAULT_PATCH_SIZE, nb_channels=5, nb_filters_start=32,
               growth_factor=2, upconv=True, droprate=0.4, class_weights=[2, 1, 2]):
    nb_filters = nb_filters_start
    inputs = Input(shape=(img_size, img_size, nb_channels))
    conv1 = Conv2D(nb_filters, (3, 3), activation='relu', padding='same')(inputs)
    conv1 = Conv2D(nb_filters, (3, 3), activation='relu', padding='same')(conv1)
    pool1 = MaxPooling2D(pool_size=(2, 2))(conv1)

    nb_filters *= growth_factor
    pool1 = BatchNormalization()(pool1)
    conv2 = Conv2D(nb_filters, (3, 3), activation='relu', padding='same')(pool1)
    conv2 = Conv2D(nb_filters, (3, 3), activation='relu', padding='same')(conv2)
    pool2 = MaxPooling2D(pool_size=(2, 2))(conv2)
    pool2 = Dropout(droprate)(pool2)

    nb_filters *= growth_factor
    pool2 = BatchNormalization()(pool2)
    conv3 = Conv2D(nb_filters, (3, 3), activation='relu', padding='same')(pool2)
    conv3 = Conv2D(nb_filters, (3, 3), activation='relu', padding='same')(conv3)
    pool3 = MaxPooling2D(pool_size=(2, 2))(conv3)
    pool3 = Dropout(droprate)(pool3)

    nb_filters *= growth_factor
    pool3 = BatchNormalization()(pool3)
    conv4_0 = Conv2D(nb_filters, (3, 3), activation='relu', padding='same')(pool3)
    conv4_0 = Conv2D(nb_filters, (3, 3), activation='relu', padding='same')(conv4_0)
    pool4_1 = MaxPooling2D(pool_size=(2, 2))(conv4_0)
    pool4_1 = Dropout(droprate)(pool4_1)

    nb_filters *= growth_factor
    pool4_1 = BatchNormalization()(pool4_1)
    conv4_1 = Conv2D(nb_filters, (3, 3), activation='relu', padding='same')(pool4_1)
    conv4_1 = Conv2D(nb_filters, (3, 3), activation='relu', padding='same')(conv4_1)
    pool4_2 = MaxPooling2D(pool_size=(2, 2))(conv4_1)
    pool4_2 = Dropout(droprate)(pool4_2)

    nb_filters *= growth_factor
    conv5 = Conv2D(nb_filters, (3, 3), activation='relu', padding='same')(pool4_2)
    conv5 = Conv2D(nb_filters, (3, 3), activation='relu', padding='same')(conv5)

    # this is the bottom part of "U"

    nb_filters //= growth_factor
    if upconv:
        up6_1 = concatenate([Conv2DTranspose(nb_filters, (2, 2), strides=(2, 2), padding='same')(conv5), conv4_1])
    else:
        up6_1 = concatenate([UpSampling2D(size=(2, 2))(conv5), conv4_1])

    up6_1 = BatchNormalization()(up6_1)
    conv6_1 = Conv2D(nb_filters, (3, 3), activation='relu', padding='same')(up6_1)
    conv6_1 = Conv2D(nb_filters, (3, 3), activation='relu', padding='same')(conv6_1)
    conv6_1 = Dropout(droprate)(conv6_1)

    nb_filters //= growth_factor
    if upconv:
        up6_2 = concatenate([Conv2DTranspose(nb_filters, (2, 2), strides=(2, 2), padding='same')(conv6_1), conv4_0])
    else:
        up6_2 = concatenate([UpSampling2D(size=(2, 2))(conv6_1), conv4_0])
    up6_2 = BatchNormalization()(up6_2)
    conv6_2 = Conv2D(nb_filters, (3, 3), activation='relu', padding='same')(up6_2)
    conv6_2 = Conv2D(nb_filters, (3, 3), activation='relu', padding='same')(conv6_2)
    conv6_2 = Dropout(droprate)(conv6_2)

    nb_filters //= growth_factor
    if upconv:
        up7 = concatenate([Conv2DTranspose(nb_filters, (2, 2), strides=(2, 2), padding='same')(conv6_2), conv3])
    else:
        up7 = concatenate([UpSampling2D(size=(2, 2))(conv6_2), conv3])
    up7 = BatchNormalization()(up7)
    conv7 = Conv2D(nb_filters, (3, 3), activation='relu', padding='same')(up7)
    conv7 = Conv2D(nb_filters, (3, 3), activation='relu', padding='same')(conv7)
    conv7 = Dropout(droprate)(conv7)

    nb_filters //= growth_factor
    if upconv:
        up8 = concatenate([Conv2DTranspose(nb_filters, (2, 2), strides=(2, 2), padding='same')(conv7), conv2])
    else:
        up8 = concatenate([UpSampling2D(size=(2, 2))(conv7), conv2])
    up8 = BatchNormalization()(up8)
    conv8 = Conv2D(nb_filters, (3, 3), activation='relu', padding='same')(up8)
    conv8 = Conv2D(nb_filters, (3, 3), activation='relu', padding='same')(conv8)
    conv8 = Dropout(droprate)(conv8)

    nb_filters //= growth_factor
    if upconv:
        up9 = concatenate([Conv2DTranspose(nb_filters, (2, 2), strides=(2, 2), padding='same')(conv8), conv1])
    else:
        up9 = concatenate([UpSampling2D(size=(2, 2))(conv8), conv1])
    conv9 = Conv2D(nb_filters, (3, 3), activation='relu', padding='same')(up9)
    conv9 = Conv2D(nb_filters, (3, 3), activation='relu', padding='same')(conv9)

    conv10 = Conv2D(nb_classes, (1, 1), activation='softmax')(conv9)

    model = Model(inputs=inputs, outputs=conv10)

    def weighted_binary_crossentropy(y_true, y_pred):
        class_loglosses = tf.reduce_mean(tf.keras.backend.binary_crossentropy(y_true, y_pred), axis=[0, 1, 2])
        return tf.reduce_sum(class_loglosses * tf.constant(class_weights))

    model.compile(optimizer=Adam(lr=0.0001),
                  loss='binary_crossentropy',
                  metrics=['accuracy', tf.keras.metrics.MeanIoU(num_classes=NB_CLASSES)])

    return model


NB_BANDS = 5
NB_CLASSES = 3
CLASS_WEIGHTS = [2, 1, 2]
NB_EPOCHS = 60
BATCH_SIZE = 8
UPCONV = True
PATCH_SIZE = 320  # should be divisible by 16
NB_TRAIN = 800
NB_VAL = 200


def normalize(img):
    minv = img.min()
    maxv = img.max()
    return 2.0 * (img - minv) / (maxv - minv) - 1.0


def get_model():
    return unet_model(NB_CLASSES, PATCH_SIZE, nb_channels=NB_BANDS, upconv=UPCONV, class_weights=CLASS_WEIGHTS)


def predict(x, model, patch_size=320, nb_classes=3):
    img_height = x.shape[0]
    img_width = x.shape[1]
    nb_channels = x.shape[2]

    # extend image so that it contains integer number of patches
    nb_patches_vertical = math.ceil(img_height / patch_size)
    nb_patches_horizontal = math.ceil(img_width / patch_size)
    extended_height = patch_size * nb_patches_vertical
    extended_width = patch_size * nb_patches_horizontal
    ext_x = np.zeros((extended_height, extended_width, nb_channels), dtype=np.float32)

    # fill extended image with mirrors
    ext_x[:img_height, :img_width, :] = x
    for i in range(img_height, extended_height):
        mirror_i = img_height - (i - img_height) % img_height - 1
        ext_x[i, :, :] = ext_x[mirror_i, :, :]

    for j in range(img_width, extended_width):
        mirror_j = img_width - (j - img_width) % img_width - 1
        ext_x[:, j, :] = ext_x[:, mirror_j, :]

    patches_list = []
    for i in range(nb_patches_vertical):
        for j in range(nb_patches_horizontal):
            x0, x1 = i * patch_size, (i + 1) * patch_size
            y0, y1 = j * patch_size, (j + 1) * patch_size
            patches_list.append(ext_x[x0:x1, y0:y1, :])

    # model.predict() requires a numpy array
    patches = np.asarray(patches_list)

    # predictions (no overlap)
    patches_predict = model.predict(patches, batch_size=4)
    prediction = np.zeros((extended_height, extended_width, nb_classes), dtype=np.float32)
    for k in range(patches_predict.shape[0]):
        i = k // nb_patches_horizontal  # Corrected: vertical index
        j = k % nb_patches_horizontal  # Corrected: horizontal index
        x0, x1 = i * patch_size, (i + 1) * patch_size
        y0, y1 = j * patch_size, (j + 1) * patch_size
        prediction[x0:x1, y0:y1, :] = patches_predict[k, :, :, :]

    return prediction[:img_height, :img_width, :]


def picture_from_mask(mask, threshold=0):
    colors = {
        0: [0, 0, 255],
        1: [255, 0, 0],
        2: [0, 255, 0],
    }
    z_order = {
        1: 0,
        2: 1,
        3: 2,
    }

    pict = 255 * np.ones((3, mask.shape[1], mask.shape[2]), dtype=np.uint8)
    for i in range(1, 4):
        cl = z_order[i]
        for ch in range(3):
            pict[ch, :, :][mask[cl, :, :] > threshold] = colors[cl][ch]
    return pict


# ==========================================================================================================
weights_path = 'unet_weights_final.hdf5'  # Set the path to your weights file
# file_path = 'Test/01.tif'    # Set the path to your TIFF file

# output_tif_folder = 'result'
# output_png_folder = 'png'

file_path = sys.argv[1] if len(sys.argv) > 1 else None
output_tif_folder = sys.argv[2] if len(sys.argv) > 2 else None
output_png_folder = sys.argv[3] if len(sys.argv) > 3 else None

input_file_base = os.path.splitext(os.path.basename(file_path))[0]

output_tiff_path = os.path.join(output_tif_folder, f"{input_file_base}_output.tif")
output_png_path = os.path.join(output_png_folder, f"{input_file_base}_output.png")

model = get_model()
model.load_weights(weights_path)

print("processing {}".format(file_path))
inp = normalize(tiff.imread(file_path).transpose([1, 2, 0]))
res = predict(inp, model, patch_size=PATCH_SIZE, nb_classes=NB_CLASSES).transpose([2, 0, 1])
res_map = picture_from_mask(res, 0.5)
tiff.imsave(output_tiff_path, res_map)

# Convert TIFF to PNG
tif_image = Image.open(output_tiff_path)
tif_image.save(output_png_path)
print("Saved PNG file at {}".format(output_png_path))
