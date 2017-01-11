pointInBoundingBox <- function(term, value, param) {
    vertices = value[[term]]
    point = value$point
    
    result = point[1] > min(vertices[,1]) && 
    point[2] < max(vertices[,2]) && 
    point[3] < max(vertices[,3]) &&
    
    point[1] < max(vertices[,1]) && 
    point[2] > min(vertices[,2]) && 
    point[3] > min(vertices[,3])
    
    if (param)
        result
    else
        !result
}

pointIsVector3f <- function(term, value, param) {
    vector = value[[term]]

    length(vector) == 3 && class(vector) == "numeric"
}