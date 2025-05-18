#!/bin/bash

# Create models directory if it doesn't exist
mkdir -p models

# Download the pre-trained model
curl -L "https://huggingface.co/connect4/connect4-model/resolve/main/connect4_model.pt" -o models/connect4_model.pt

echo "Model downloaded successfully!" 