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

export function setCaretPosition(ctrl, pos) {
    // Modern browsers
    if (ctrl.setSelectionRange) {
        ctrl.focus();
        ctrl.setSelectionRange(pos, pos);

        // IE8 and below
    } else if (ctrl.createTextRange) {
        var range = ctrl.createTextRange();
        range.collapse(true);
        range.moveEnd('character', pos);
        range.moveStart('character', pos);
        range.select();
    }
}

export function convertPixelsToRem(px) {
    return px / parseFloat(getComputedStyle(document.documentElement).fontSize);
}
