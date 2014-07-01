/**
 * Compare trees and adds the leave nodes, if numeric.
 * Branches that are not common to both nodes are not included in the final result
 */
var TreeOperator = new Object();

TreeOperator.add = function(t1, t2) {
	if(t1 instanceof Array && t2 instanceof Array) {
		var result = new Array();
		var length = t1.length>t2.length?t2.length:t1.length;
		for(var i = 0; i<length ; i++) {
			result[i] = this.add(t1[i],t2[i]);
		}
		return result;
	} else if(t1 instanceof Object && t2 instanceof Object) {
		var result = {};
		for(var key in t1) {
			if(t2.hasOwnProperty(key)) {
				var res = this.add(t1[key],t2[key]);
				if(res != null && res != undefined) {
					result[key] = res;
				}
			}
		}
		return result;
	} else if(!isNaN(Number(t1)) && !isNaN(Number(t2))) {
		return Number(t1)+Number(t2);
	}
}

/**
 * Multiply by a scalar all the leave nodes that are numeric
 */
TreeOperator.muls = function(obj, scalar) {
	if(obj instanceof Array) {
		var result = new Array();
		for(var i = 0 ; i < obj.length ; i++) {
			result[i] = this.muls(obj[i], scalar);
		}
		return result;
	} else if(obj instanceof Object) {
		var result = {};
		for(var key in obj) {
			result[key] = this.muls(obj[key],scalar);
		}
		return result;
	} else if(!isNaN(Number(obj))) {
		return Number(obj)*scalar;
	} else {
		return obj;
	}
}

/**
 * Divide all the leave nodes by a scalar
 */

TreeOperator.divs = function(obj, scalar) {
	return this.muls(obj,1/scalar);
}

/*
 * Returns a vector where the elements are:
 * 		vec[0] : the summatory of all the trees in the vector array between indexes begin (inclusive) and end (exclusive)
 * 		vec[1] : the number of trees that have been summed in vec[0]
 */
TreeOperator.sum = function(tarray,begin,end) {
	if(!begin) begin = 0;
	if(!end) end = tarray.length;

	if(begin == end-1) {
		return [ tarray[begin], 1];
	} else {
		var split = Math.floor((begin+end)/2);
		var part1 = this.sum(tarray,begin,split);
		var part2 = this.sum(tarray,split,end);
		return [ this.add(part1[0],part2[0]), part1[1]+part2[1] ];
	}
}

module.exports = TreeOperator;
