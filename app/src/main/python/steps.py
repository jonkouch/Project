import numpy as np
from scipy.signal import find_peaks
import pandas as pd

samples_array = []

def max_acc(sample):
    global samples_array
    samples_array.append(sample)
    peaks = (find_peaks(samples_array, height = 5, distance = 5)[0])

    np_samples_array = np.array(samples_array)

    top = np.max(np_samples_array[peaks])

    return top



def step_number(sample):
    global samples_array
    samples_array.append(sample)
    num_of_peaks = len(find_peaks(samples_array, height = 12, distance = 5)[0])
    return num_of_peaks


def reset():
    global samples_array
    samples_array = []