const rhymesKey = 'rhymes';
const latestIndexKey = 'latest-index';
const wordsIdxDictKey = 'words';
const idxWordsDictKey = 'indexes';

const loadedRhymes = 1;
const calculatedRhymes = 2;


export function setRhymes(word, rhymes = [], stressDependent) {
    const idxesData = getIdxWordsData();

    const idx = getIdx(word, idxesData);
    const rhymesData = getRhymesData();
    if (!rhymesData[idx]) {
        rhymesData[idx] = {};
    }

    rhymes.forEach(r => {
        const id = getIdx(r, idxesData);

        if (!rhymesData[id]) {
            rhymesData[id] = {};
        }
        if (id !== idx) {
            if (!stressDependent) {
                rhymesData[idx][id] = loadedRhymes;
            }
            rhymesData[id][idx] = loadedRhymes;
        }

        rhymes.forEach((r2 => {
            const id2 = getIdx(r2, idxesData);
            if (!rhymesData[id2]) {
                rhymesData[id2] = {};
            }
            if (id !== id2) {
                rhymesData[id2][id] = loadedRhymes;
                rhymesData[id][id2] = loadedRhymes;
            }
        }));
    });

    localStorage.setItem(rhymesKey, JSON.stringify(rhymesData));
}

export function getRhymes(word) {
    const idx = getIdx(word);
    const rhymesData = getRhymesData();
    const rhymes = rhymesData[idx] || {};
    const rhymesIdxs = Object.keys(rhymes);
    const wasLoaded = rhymesIdxs.reduce((val, i) => {
        return val || rhymes[i] === loadedRhymes
    }, false)
    return {
        rhymes: rhymesIdxs.map(i => getWord(i)).sort(),
        needToLoad: !wasLoaded || rhymesIdxs.length === 0
    }
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

function getIdx(word, idxes = getIdxWordsData()) {
    let idx = idxes[word];
    if (!idx) {
        idx = getLatestIdx() ? getLatestIdx() + 1 : 1;
        setLatestIdx(idx);
        idxes[word] = idx;

        let words = getWordsIdxData();
        words[idx] = word;

        setIdxWordsData(idxes);
        setWordsIdxData(words);
    }
    return idx;
}

function getLatestIdx() {
    return parseInt(localStorage.getItem(latestIndexKey), 10);
}

function setLatestIdx(value) {
    localStorage.setItem(latestIndexKey, value);
}