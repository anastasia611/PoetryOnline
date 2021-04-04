export function isLetter(str) {
    if (!str) return false;
    return str.length === 1 && str.match(/[a-zA-zа-яА-Я]/i);
}

export function isPunctuationMark(str) {
    if (!str) return false;
    return str.length === 1 && str.match(/[ !?:;.,"()-]/i);
}