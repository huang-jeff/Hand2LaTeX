# Mute tensorflow debugging information on console
import os
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'

from scipy.misc import imsave, imread, imresize
import numpy as np
import argparse
from keras.models import model_from_yaml
import re
import base64
import pickle
from PIL import Image, ImageFilter
from resizeimage import resizeimage

def load_model(bin_dir):
    ''' Load model from .yaml and the weights from .h5

        Arguments:
            bin_dir: The directory of the bin (normally bin/)

        Returns:
            Loaded model from file
    '''

    # load YAML and create model
    yaml_file = open('%s/model.yaml' % bin_dir, 'r')
    loaded_model_yaml = yaml_file.read()
    yaml_file.close()
    model = model_from_yaml(loaded_model_yaml)

    # load weights into new model
    model.load_weights('%s/model.h5' % bin_dir)
    return model

def imageprepare(argv):
  """
  This function returns the pixel values.
  The imput is a png file location.
  """
  img = Image.open(argv)
  img = img.filter(ImageFilter.GaussianBlur(radius=2))
  newImage = resizeimage.resize_contain(img, [64, 64]).convert('L')
  newImage = newImage.point(lambda x: 0  if x<160 else 255, '1')
  
  #newImage.save("sample.png")

  tv = list(newImage.getdata()) #get pixel values
  
  #normalize pixels to 0 and 1. 0 is pure white, 1 is pure black.
  tva = [ x for x in tv] 
  tva = np.reshape(tva, (64, 64))
  return tva


def predict(path, model, mapping):
    ''' Called when user presses the predict button.
        Processes the canvas and handles the image.
        Passes the loaded image into the neural network and it makes
        class prediction.
    '''

    # read parsed image back in 8-bit, black and white mode (L)
    x = imageprepare(path)
    ### Experimental
    # Crop on rows
    # x = crop(x)
    # x = x.T
    # Crop on columns
    # x = crop(x)
    # x = x.T

    # Visualize new array
    x = imresize(x,(28,28))
    x = np.invert(x)

    # reshape image data for use in neural network
    x = x.reshape(1,28,28,1)

    # Convert type to float32
    x = x.astype('float32')

    # Normalize to prevent issues with model
    x /= 255

    # Predict from model
    out = model.predict(x)

    # Generate response
    try:
      response = {'prediction': chr(mapping[(int(np.argmax(out, axis=1)[0]))]),
                'confidence': str(max(out[0]) * 100)[:6]}
    except:
      print(out)
      response ={'prediction': '?'}

    print(response)
    return response['prediction']

if __name__ == '__main__':
    # Parse optional arguments
    parser = argparse.ArgumentParser(description='A webapp for testing models generated from training.py on the EMNIST dataset')
    parser.add_argument('--bin', type=str, default='bin', help='Directory to the bin containing the model yaml and model h5 files')
    parser.add_argument('--host', type=str, default='0.0.0.0', help='The host to run the flask server on')
    parser.add_argument('--port', type=int, default=5000, help='The port to run the flask server on')
    args = parser.parse_args()

    # Overhead
    model = load_model(args.bin)
    mapping = pickle.load(open('%s/mapping.p' % args.bin, 'rb'))
    predict()