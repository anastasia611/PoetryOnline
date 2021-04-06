const punct = /[!"-:;—?-]/i;
const letters = /[a-zA-zа-яА-Я]/i;

export function isLetter(str) {
    if (!str) return false;
    return str.length === 1 && str.match(letters);
}

export function isPunctuationMark(str) {
    if (!str) return false;
    return str.length === 1 && str.match(punct);
}

export function parseWords(str) {
    if (!str) return false;
    return str.split(/\s+|(,|-|—|\.|;|:|\?|!|\(|\))/).filter(Boolean);
}