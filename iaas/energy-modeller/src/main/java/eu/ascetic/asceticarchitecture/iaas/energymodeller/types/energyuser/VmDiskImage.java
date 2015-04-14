/**
 * Copyright 2015 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser;

import java.io.File;
import java.util.Objects;

/**
 * This records the VMs disk images that make up the VM. The aim of this is to
 * better characterise the VM. All VMs that are created from the same set of
 * disk images are likely to have the same applications and therefore are more
 * likely to have similar workload profiles, then VMs with unrelated disk
 * images.
 *
 * @author Richard Kavanagh
 */
public class VmDiskImage {

    private String diskImage;
    private String parentImage;

    /**
     * This creates a VM disk image object.
     *
     * @param diskImage The disk image to set
     */
    public VmDiskImage(String diskImage) {
        this.diskImage = diskImage;
        this.parentImage = null;
    }
    
    /**
     * This creates a VM disk image object.
     *
     * @param diskImage The disk image to set
     * @param parentImage The disk image's parent file.
     */
    public VmDiskImage(String diskImage, String parentImage) {
        this.diskImage = diskImage;
        this.parentImage = parentImage;
    }

    /**
     * This gets the file representation of this disk image.
     *
     * @return the diskImage
     */
    public String getDiskImage() {
        return diskImage;
    }

    /**
     * This sets the file representation of this disk image.
     *
     * @param diskImage the diskImage to set
     */
    public void setDiskImage(String diskImage) {
        this.diskImage = diskImage;
    }

    /**
     * This gets the file representation of this disk image's parent should one
     * exist. This returns null if it does not.
     *
     * @return the images parent image should one exist.
     */
    public String getParentImage() {
        return parentImage;
    }

    /**
     * This sets the file representation of this disk image's parent should one
     * exist. This returns null if it does not.
     *
     * @param parentImage the images parent image should one exist.
     */
    public void setParentImage(String parentImage) {
        this.parentImage = parentImage;
    }

    /**
     * If the image has a parent or not.
     *
     * @return True if the disk image has a parent image, otherwise false.
     */
    public boolean hasParent() {
        return (parentImage != null);
    }
    
    /**
     * This checks to see if the disk image exists or not.
     * @return True if the disk image exists, otherwise false.
     */
    public boolean diskImageExists() {
        return new File(diskImage).exists();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass().equals(VmDiskImage.class)) {
            VmDiskImage img = (VmDiskImage) obj;
            String imgPath = img.getDiskImage();
            return (diskImage.equals(imgPath));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.diskImage);
        return hash;
    }

    @Override
    public String toString() {
        return diskImage;
    }

}
