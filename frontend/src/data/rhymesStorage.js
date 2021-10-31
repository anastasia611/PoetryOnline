const rhymesKey = 'rhymes';

export function set(word, rhymes) {
    const rhymesData = getData();
    rhymesData[word] = rhymes;
    localStorage.setItem(rhymesKey, JSON.stringify(rhymesData));
}

export function get(word) {
    const rhymesData = getData();
    return rhymesData[word];
}

function getData() {
    return JSON.parse(localStorage.getItem(rhymesKey) || '{}');
}
