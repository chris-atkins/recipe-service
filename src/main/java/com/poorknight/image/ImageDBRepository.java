package com.poorknight.image;

public abstract class ImageDBRepository {
    abstract Image saveNewImage(Image image);

    abstract Image findImage(String imageId);

    abstract void deleteImage(String imageId);
}
