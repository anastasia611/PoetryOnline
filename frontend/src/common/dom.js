export function getCaretCharacterOffsetWithin(element) {
    const doc = element.ownerDocument || element.document;
    const win = doc.defaultView || doc.parentWindow;

    let caretOffset = 0;
    let sel;

    if (typeof win.getSelection != 'undefined') {
        sel = win.getSelection();

        if (sel.rangeCount > 0) {
            const range = win.getSelection().getRangeAt(0);
            const preCaretRange = range.cloneRange();

            preCaretRange.selectNodeContents(element);
            preCaretRange.setEnd(range.endContainer, range.endOffset);
            caretOffset = preCaretRange.toString().length;
            console.log(preCaretRange.toString(), caretOffset);
        }
    } else if ((sel = doc.selection) && sel.type !== "Control") {
        const textRange = sel.createRange();
        const preCaretTextRange = doc.body.createTextRange();

        preCaretTextRange.moveToElementText(element);
        preCaretTextRange.setEndPoint("EndToEnd", textRange);
        caretOffset = preCaretTextRange.text.length;
    }
    return caretOffset;
}

export function setCaretPosition(el, pos) {
    const range = document.createRange();
    const sel = window.getSelection();

    if (el.firstChild) {
        console.log('SET CAR', pos)

        range.setStart(el.firstChild, pos);
        range.collapse(true);

        sel.removeAllRanges();
        sel.addRange(range);
    }
}

export function convertPixelsToRem(px) {
    return px / parseFloat(getComputedStyle(document.documentElement).fontSize);
}

export function isPortraitOrientation() {
    return window.innerHeight > window.innerWidth;
}
