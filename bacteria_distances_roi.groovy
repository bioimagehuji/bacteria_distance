// QuPath script to detect and compute the distances from every bacterium centroid to the closest nucleus border.
// Usage: Draw a new annotation and run the script.
// Notes:
//   Bacteria inside a nucleus have a negative distance to it border.
//   To compute the distance distribution: Measure -> Show detection measurements. Use the column named: "Signed distance to annotation Nucleus µm".


def user_annotation = getSelectedObject()
def nuc_detections = PathObjects.createAnnotationObject(user_annotation.getROI())
def nuc_annotations = PathObjects.createAnnotationObject(user_annotation.getROI())
nuc_detections.setLocked(true)
nuc_annotations.setLocked(true)
//nuc_detections.setName("nuceli_detections")
//nuc_annotations.setName("nuceli_annotations")
user_annotation.addChildObject(nuc_annotations)
user_annotation.addChildObject(nuc_detections)

// Detect cell nuclei from DAPI
selectObjects(nuc_detections)
runPlugin('qupath.imagej.detect.cells.WatershedCellDetection', '{"detectionImage":"LED-DAPI_Q","requestedPixelSizeMicrons":0.5,"backgroundRadiusMicrons":8.0,"backgroundByReconstruction":true,"medianRadiusMicrons":0.0,"sigmaMicrons":1.5,"minAreaMicrons":10.0,"maxAreaMicrons":400.0,"threshold":10.0,"watershedPostProcess":true,"cellExpansionMicrons":0.1,"includeNuclei":true,"smoothBoundaries":true,"makeMeasurements":false}')
for (PathObject childObject : nuc_detections.getChildObjects()) {
    childObject.setPathClass(getPathClass("Nucleus"))
    nucleus_ann = PathObjects.createAnnotationObject(childObject.getROI(), childObject.getPathClass())
    nucleus_ann.setLocked(true)
    nuc_annotations.addChildObject(nucleus_ann)
}
user_annotation.removeChildObject(nuc_detections)


// Duplicate nuclei rectangle annotation (without nuclei)
def bac_annotation = PathObjects.createAnnotationObject(user_annotation.getROI())
//bac_annotation.setName('bacteria_annotation');
user_annotation.addChildObject(bac_annotation)


// Detect bacteria from TRITC
selectObjects(bac_annotation)
runPlugin('qupath.imagej.detect.cells.WatershedCellDetection', '{"detectionImage":"LED-Tritc_Q (C3)","requestedPixelSizeMicrons":0.1,"backgroundRadiusMicrons":32.0,"backgroundByReconstruction":true,"medianRadiusMicrons":0.0,"sigmaMicrons":0.0,"minAreaMicrons":0.5,"maxAreaMicrons":10.0,"threshold":90.0,"watershedPostProcess":true,"cellExpansionMicrons":0.1,"includeNuclei":true,"smoothBoundaries":true,"makeMeasurements":false}')
for (PathObject childObject : bac_annotation.getChildObjects()) {
    childObject.setPathClass(getPathClass("Bacterium"))
}

// Compute all ditances between nucleus centers and the closest annnotation border, with negative sign.
detectionToAnnotationDistancesSigned(false)
// Color bacteria according to distance
setCellIntensityClassifications("Signed distance to annotation Nucleus µm", 0.0, 1.0, 5.0)