export function push(array, index, value) {
    return array.slice(0, index).concat([value]).concat(array.slice(index, array.length));
}

export function remove(array, index) {
    array.splice(index, 1);
}