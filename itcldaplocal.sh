#!/bin/sh

ssh -f -N -L 127.1:3899:msk-ad.itc.itc-electronics.com:389 \
  developer@94.230.57.30
