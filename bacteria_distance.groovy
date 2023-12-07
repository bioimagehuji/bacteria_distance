// This QuPath script detects and computes the distances between bacteria and nuclei.
// To compute the distance distribution: Measure -> Show detection measurements. Right click a row. Show classes -> bacteria. This filters to only bacteria.
// Use the columns named: "Signed distance to annotation bacteria µm" and "Signed distance to annotation nuclei µm"


clearAllObjects();

// Detect cell nuclei from DAPI
createFullImageAnnotation(true)
getSelectedObject().setPathClass(getPathClass("nuclei"))
// Note: detect Nuclei as cell objects with "cellExpansionMicrons":0.1, because detectionCentroidDistances does not work on "detections" (nuclei) but only on "cells".
runPlugin('qupath.imagej.detect.cells.WatershedCellDetection', '{"detectionImage":"Dapi","requestedPixelSizeMicrons":0.5,"backgroundRadiusMicrons":8.0,"backgroundByReconstruction":true,"medianRadiusMicrons":0.0,"sigmaMicrons":1.5,"minAreaMicrons":10.0,"maxAreaMicrons":400.0,"threshold":100.0,"watershedPostProcess":true,"cellExpansionMicrons":0.1,"includeNuclei":true,"smoothBoundaries":true,"makeMeasurements":true}')

// Detect bacteria from CY5
createFullImageAnnotation(true)
getSelectedObject().setPathClass(getPathClass("bacteria"))
// Note: detect bacteria as cells objects with "cellExpansionMicrons":0.1, because detectionCentroidDistances does not work on "detections" (nuclei) but only on "cells".
runPlugin('qupath.imagej.detect.cells.WatershedCellDetection', '{"detectionImage":"CY5","requestedPixelSizeMicrons":0.5,"backgroundRadiusMicrons":0.0,"backgroundByReconstruction":true,"medianRadiusMicrons":0.0,"sigmaMicrons":0.0,"minAreaMicrons":1.0,"maxAreaMicrons":100.0,"threshold":260.0,"watershedPostProcess":true,"cellExpansionMicrons":0.1,"includeNuclei":true,"smoothBoundaries":true,"makeMeasurements":true}')


// Mark each cell as nuclei/bacteria according to its annnotation class
getCellObjects().each{
    if(it.getParent().getPathClass() == getPathClass("nuclei")){
        it.setPathClass(getPathClass("nuclei"))
    }
    if(it.getParent().getPathClass() == getPathClass("bacteria")){
        it.setPathClass(getPathClass("bacteria"))
    }
}

// Delete Border annnotations to avoid calculating distancees to border.
selectAnnotations();
clearSelectedObjects();

// Convert and copy detections to annnotations
def detections = getDetectionObjects()
def newAnnotations = detections.collect {
    return PathObjects.createAnnotationObject(it.getROI(), it.getPathClass())
}
addObjects(newAnnotations)

// Compute all ditances between nucleus centers and the closest annnotation border, with negative sign.
detectionToAnnotationDistancesSigned(false)

// Delete copied annotations and leave the original detections.
selectAnnotations();
clearSelectedObjects();
