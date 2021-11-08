const rhymesKey = 'rhymes';
const currentIndexKey = 'current-index';
const wordsIdxDictKey = 'words';
const idxWordsDictKey = 'indexes';


export function setRhymes(word, rhymes = []) {
    word = word.toLowerCase();
    rhymes = rhymes.map(r => r.toLowerCase());

    const idx = getIdx(word);
    const rhymesData = getRhymesData();
    if (!rhymesData[idx]) {
        rhymesData[idx] = {};
    }

    // rhymes.forEach((r, i) => {
    //     const id = getIdx(r.toLowerCase());
    //     if (!rhymesData[id]) {
    //         rhymesData[id] = {};
    //     }
    //     rhymesData[id][idx] = 1;
    //     rhymesData[idx][id] = 1;
    //
    //     rhymes.forEach(((r2, j) => {
    //         const id2 = getIdx(r2.toLowerCase());
    //         if (!rhymesData[id2]) {
    //             rhymesData[id2] = {};
    //         }
    //         if (i !== j) {
    //             rhymesData[id2][id] = 1;
    //             rhymesData[id][id2] = 1;
    //         }
    //     }));
    // });

    localStorage.setItem(rhymesKey, JSON.stringify(rhymesData));
}

export function getRhymes(word) {
    word = word.toLowerCase();

    const idx = getIdx(word);
    const rhymesData = getRhymesData();
    return Object.keys(rhymesData[idx] || {}).map(i => getWord(i));
}

function getRhymesData() {
    return JSON.parse(localStorage.getItem(rhymesKey) || '{}');
}

function getWordsIdxData() {
    return JSON.parse(localStorage.getItem(wordsIdxDictKey) || '{}');
}

function getIdxWordsData() {
    return JSON.parse(localStorage.getItem(idxWordsDictKey) || '{}');
}

function setWordsIdxData(data) {
    return localStorage.setItem(wordsIdxDictKey, JSON.stringify(data));
}

function setIdxWordsData(data) {
    return localStorage.setItem(idxWordsDictKey, JSON.stringify(data));
}

function getWord(idx) {
    return getWordsIdxData()[idx];
}

function getIdx(word) {
    const idxes = getIdxWordsData();

    let idx = idxes[word];
    if (!idx) {
        idx = getCurrentIdx() ? getCurrentIdx() + 1 : 1;
        setCurrentIdx(idx);
        idxes[word] = idx;

        let words = getWordsIdxData();
        words[idx] = word;

        setIdxWordsData(idxes);
        setWordsIdxData(words);
    }
    return idx;
}

function getCurrentIdx() {
    return parseInt(localStorage.getItem(currentIndexKey), 10);
}

function setCurrentIdx(value) {
    localStorage.setItem(currentIndexKey, value);
}