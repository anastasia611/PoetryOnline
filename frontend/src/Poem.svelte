<script>
    import { createEventDispatcher } from 'svelte';
    import IconButton from "./IconButton.svelte";
    import Word from "./Word.svelte";
    import { push, remove } from "./common/arrays";

    export let stanzas = [];

    export let stanzaIndex;
    export let lineIndex;
    export let wordIndex;

    const dispatch = createEventDispatcher();

    const title = 'Стихотворение';
    const removeLineTitle = 'Удалить строку';


    const onFocus = (s, l, w) => {
        setIndexes(s, l, w);
        console.log('foc')
    };

    const onBlur = (e, s, l, w) => {
        // const word = e.detail.word;
        // if (!word) {
        //     console.log('rm in word')
        //     // onRemoveWord();
        // }
        // const stanza = getStanza(s);
        // const line = getLine(s, l);
        //
        // if (!line || !line.length || !getWord(0, l, s)) {
        //     removeLine();
        // }
        // if (!stanza || !stanza.length || !getLine(0, s)) {
        //     removeStanza();
        // }
    };

    // TODO: merge lines
    const onRemoveBeforeWord = (s, l, w) => {
        setIndexes(s, l, w);
        if (l === 0) {
            mergeStanzas(s);
        } else {
            mergeLines(s, l);
        }
    };

    const onRemoveAfterWord = (s, l, w) => {
        setIndexes(s, l, w);
        const stanza = getStanza();
        if (l === stanza.length - 1) {
            mergeStanzas(s + 1);
        }
    };

    const onRemoveWord = (s, l, w) => {
        setIndexes(s, l, w);
        const words = getLine();

        removeWord();

        if (!words.length) {
            console.log('rm line - empty on rem word', words)
            onRemoveLine(s, l);
        } else if (wordIndex === words.length) { //???
            onBackWord(s, l, w);
        }
    };

    const onRemoveLine = (s, l) => {
        setIndexes(s, l);
        console.log('rm line', getStanza(), l)
        removeLine();
        if (!getStanza().length) {
            removeStanza();
        } else {
            onBackLine(s, l, wordIndex);
        }
    };

    const onEnter = (e, s, l, w) => {
        setIndexes(s, l, w);
        const words = getLine();
        const word = e.detail.word;
        updateWord(word);

        // TODO: divide bef / aft word cases
        if (w < words.length - 1) {
            console.log('enter word', words, word, w);
            splitLine(s, l, w);
            //setWordIndex(++w);
        } else {
            console.log('enter word - new line', words, word, w)
            onAddLine(s, l);
        }
    };

    const onSpace = (e, s, l, w) => {
        setIndexes(s, l, w);
        const words = getLine();
        const word = e.detail.word;
        updateWord(word);

        console.log('space', words, word, wordIndex)

        if (!word) {
            return;
        }
        if (wordIndex < words.length - 1 && !getWord(wordIndex + 1)) {
            return;
        }
        addWord('');
    };

    const onPunct = (e, s, l, w) => {
        setIndexes(s, l, w);
        const word = e.detail.word;
        updateWord(word);
        addWord(e.detail.sign);
        addWord('');
    };

    const onNextWord = (s, l, w) => {
        setIndexes(s, l, w);
        const words = getLine();

        console.log('next word', w, w + 1, words)
        if (w < words.length - 1) {
            setWordIndex(++w);
        } else {
            onNextLine(s, l, w);
        }
    };

    const onNextLine = (s, l, w) => {
        setIndexes(s, l, w);
        const lines = getStanza();

        console.log('next line', lines, l)
        if (l < lines.length - 1) {
            setLineIndex(++l);
            setWordIndex(0);
        } else {
            onNextStanza(s, l, w);
        }
    };

    const onNextStanza = (s, l, w) => {
        setIndexes(s, l, w);
        if (s < stanzas.length - 1) {
            setStanzaIndex(++s);
            setLineIndex(0);
            setWordIndex(0);
            console.log('nxt')
        }
    };

    const onBackWord = (s, l, w) => {
        setIndexes(s, l, w);
        const words = getLine();

        console.log('back', w, w - 1, words)
        if (w > 0) {
            setWordIndex(--w);
        } else {
            onBackLine(s, l, w);
        }
    };

    const onBackLine = (s, l, w) => {
        setIndexes(s, l, w);
        const lines = getStanza();

        console.log('back to line', lines, l)
        if (l > 0) {
            setLineIndex(--l);
            setWordIndex(getLine().length - 1);
        } else {
            onBackStanza(s, l, w);
        }
    };

    const onBackStanza = (s, l, w) => {
        setIndexes(s, l, w);
        if (s > 0) {
            setStanzaIndex(--s);
            const stanza = getStanza();

            setLineIndex(stanza.length - 1);
            const line = getLine();

            setWordIndex(line.length - 1);
            console.log('bc', stanzaIndex, lineIndex, wordIndex)
        }
    };

    const onUp = (s, l, w) => {
        setIndexes(s, l, w);
        onBackLine(s, l, w);
    };

    const onDown = (s, l, w) => {
        setIndexes(s, l, w);
        onNextLine(s, l, w);
    };

    const onAddStanza = (lines, s) => {
        setIndexes(s);
        console.log('add st', s, getStanza());

        if (getStanza() && getStanza().length && getLine(0)) {
            addStanza(lines, s);
        }
    };

    const updateWord = (word, w = wordIndex, l = lineIndex, s = stanzaIndex) => {
        if (word) {
            console.log('add word', getLine(), word, w)
            setWord(word, w, l, s);
        } else {
            console.log('add empty word - remove', getLine(), w)
            removeWord();
            if (w > 0) {
                setWordIndex(--w);
            }
        }
    };

    // change args order
    const onAddLine = (s = stanzaIndex, l, value = [ '' ]) => {
        setIndexes(s, l);
        const line = getLine();

        console.log('add line', line);

        if (!line || !line.length || !getWord(0, l)) {
            console.log('rm line', line, l)
            removeLine();
            if (!getStanza(s).length) {
                removeStanza(s);
                setStanzaIndex(stanzaIndex + 1);
                setLineIndex(0);
            } else if (lineIndex === getStanza().length) {
                setLineIndex(--l);
                onAddStanza([ [ '' ] ], s);
            } else {
                splitStanza(s, l);
            }
        } else {
            addLine(value);
            setWordIndex(0);
        }
    };

    const splitStanza = (s, l) => {
        const stanza = getStanza(s);
        const stanza1 = stanza.slice(0, l);
        const stanza2 = stanza.slice(l);
        onAddStanza(stanza1, s);
        onAddStanza([ [ '' ] ], s + 1);  // bad pattern
        onAddStanza(stanza2, s + 2);
        removeStanza(s);
        setIndexes(s + 1, 0, 0);
    };

    const mergeStanzas = s => {
        const stanza1 = getStanza(s - 1);
        const stanza2 = getStanza(s);
        const stanza = stanza1.concat(stanza2);
        onAddStanza(stanza, s - 1);
        removeStanza(s - 1);
        removeStanza(s);
        setIndexes(s - 1, stanza1.length, 0);
    };

    const splitLine = (s, l, w) => {
        const line = getLine(l, s);
        const line1 = line.slice(0, w);
        const line2 = line.slice(w, line.length ? line.length : 1);
        onAddLine(s, l, line1);
        onAddLine(s, l + 1, line2);
        removeLine(l);
        setIndexes(s, l + 1, 0);
    };

    const mergeLines = (s, l) => {
        const line1 = getLine(l - 1, s);
        const line2 = getLine(l, s);
        const line = line1.concat(line2);
        onAddLine(s, l - 1, line);
        removeLine(l - 1, s);
        removeLine(l, s);
        setIndexes(s, l - 1, line1.length);
    };

    const onGetRhymes = async (s, l) => {
        const word = getLastWord(l, s);
        /*let response = await fetch(`${URL}?word=${word}`);
        if (response.ok) {
            rhymes = await response.json();
        } else {
            console.log("Ошибка HTTP: " + response.status);
        }*/
    };

    const getStanza = (s = stanzaIndex) => {
        return stanzas[s];
    };

    const setStanza = (lines, s = stanzaIndex) => {
        stanzas[s] = lines;
    };

    const addStanza = (lines = [ [ '' ] ], s = stanzaIndex) => {
        setStanzaIndex(++s);
        setIndexes(s, 0, 0);
        stanzas = push(stanzas, s, lines);
    };

    const removeStanza = (s = stanzaIndex) => {
        remove(stanzas, s);
        setStanzaIndex(s > 0 ? --s : 0);
        if (getStanza().length) {
            setLineIndex(getStanza().length - 1);
        }
    };

    const getLine = (l = lineIndex, s = stanzaIndex) => {
        const stanza = getStanza(s);
        if (stanza) {
            return stanza[l];
        }
    };

    const setLine = (words, l = lineIndex, s = stanzaIndex) => {
        const stanza = getStanza(s);
        if (stanza) {
            stanzas[s][l] = words;
        }
    };

    const addLine = (words = [ '' ], l = lineIndex, s = stanzaIndex) => {
        setLineIndex(++l);
        setStanza(push(getStanza(s), l, words));
    };

    const removeLine = (l = lineIndex, s = stanzaIndex) => {
        const stanza = getStanza(s);
        if (stanza) {
            console.log('rm ln', s, l, getStanza(s));
            remove(getStanza(s), l);
            setStanza((getStanza())); // hack
        }
    };

    const getWord = (w = wordIndex, l = lineIndex, s = stanzaIndex) => {
        const line = getLine(l, s);
        if (line) {
            return line[w];
        }
    };

    const getLastWord = (l = lineIndex, s = stanzaIndex) => {
        const line = getLine(l, s);
        if (line) {
            return line[line.length - 1];
        }
    };

    const setWord = (word, w = wordIndex, l = lineIndex, s = stanzaIndex) => {
        const line = getLine(l, s);
        if (line) {
            line[w] = word;
        }
    };

    const addWord = (word, w = wordIndex) => {
        setWordIndex(++w);
        setLine(push(getLine(), w, word));
    };

    const removeWord = (w = wordIndex, l = lineIndex, s = stanzaIndex) => {
        const line = getLine(l, s);
        if (line) {
            remove(line, w);
            stanzas = stanzas;
        }
    };

    const setIndexes = (s, l, w) => {
        console.log('idx', s, l, w);
        setWordIndex(w);
        setLineIndex(l);
        setStanzaIndex(s);
    };

    const setWordIndex = w => {
        if (typeof w !== 'undefined') {
            wordIndex = w;
        }
    };

    const setLineIndex = l => {
        if (typeof l !== 'undefined') {
            lineIndex = l;
        }
    };

    const setStanzaIndex = s => {
        if (typeof s !== 'undefined') {
            stanzaIndex = s;
        }
    };

    const resetIndexes = () => {
        setIndexes(-1, -1, -1);
    };

</script>

<div class="poem">
    <h2 contenteditable>
        {title}
    </h2>
    {#each stanzas as lines, s}
        <div class="stanza">
            {#each lines as words, l}
                <div class="line">
                    <div>
                        {#each words as word, w}
                            <Word
                                    {word}
                                    editable={s === stanzaIndex && l === lineIndex && w === wordIndex}
                                    on:focus={() => onFocus(s, l, w)}
                                    on:blur={e => onBlur(e, s, l, w)}
                                    on:remove={() => onRemoveWord(s, l, w)}
                                    on:remove-before={() => onRemoveBeforeWord(s, l, w)}
                                    on:remove-after={() => onRemoveAfterWord(s, l, w)}
                                    on:enter={e => onEnter(e, s, l, w)}
                                    on:space={e => onSpace(e, s, l, w)}
                                    on:punct={e => onPunct(e, s, l, w)}
                                    on:back={() => onBackWord(s, l, w)}
                                    on:next={() => onNextWord(s, l, w)}
                                    on:up={() => onUp(s, l, w)}
                                    on:down={() => onDown(s, l, w)}
                                    on:editFinished/>
                        {/each}
                    </div>

                    <div class="right-menu">
                        <IconButton icon="cross" size="10" title={removeLineTitle} on:click={() => onRemoveLine(s, l)}/>
                        <IconButton icon="plus" size="11" title={removeLineTitle} on:click={() => onAddLine(s, l)}/>
                        <!--                        <button on:click={() => onGetRhymes(s, l)}>Get rhyme</button>-->
                    </div>
                </div>
            {/each}
        </div>
    {/each}
</div>

<style lang="scss">
    .poem {
        display: flex;
        width: fit-content;
        flex-direction: column;
        margin-left: auto;
        margin-right: auto;
        padding: 1rem;
    }

    .stanza {
        margin-bottom: 2rem;
        padding: 0.5rem;

        &:hover, &:focus-within {
            background-color: #F5F5F5;
        }
    }

    .line {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0.5rem 0.5rem;

        .right-menu {
            display: flex;
            align-items: center;
            float: right;
            margin-left: 1rem;
            opacity: 0;
        }

        &:hover, &:focus, &:focus-within {
            background-color: #E5E5E5;

            .right-menu {
                opacity: 1;
            }
        }
    }

    h2 {
        margin-left: 0.75rem;
    }
</style>