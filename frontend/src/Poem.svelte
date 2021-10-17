<script>
    // TODO: on click line - create new word
    import IconButton from "./IconButton.svelte";
    import Word from "./Word.svelte";
    import { getRhymes } from "./api/rhymes";
    import { clickOutside } from './actions/clickOutside';
    import { isPunctuationMark } from "./common/strings";
    import { PoemDataStore } from "./data/PoemStore";
    import Tooltip from "./Tooltip.svelte";

    export let stanzas = [];

    export let stanzaIndex = -1;
    export let lineIndex = -1;
    export let wordIndex = -1;
    let wordPos = 0;

    const title = 'Silentium';
    const addLineTitle = 'Добавить строку';
    const removeLineTitle = 'Удалить строку';
    const getRhymeTitle = 'Подобрать рифму';
    const chooseTargetTooltip = 'Выберите слово, к которому нужно подобрать рифму';
    const chooseRhymeTooltip = 'Выберите рифму из списка';
    const chooseStressTooltip = 'Укажите ударение';
    const noRhymesTooltip = 'Рифмы не найдены';
    const noRhymes = 'Не найдено';

    let chooseWord = false;
    let targetLineIndex = -1;
    let targetStanzaIndex = -1;
    let chosenLineIndex = -1;
    let chosenWordIndex = -1;

    let rhymesGot = false;
    let chooseStress = false;
    let rhymes;
    let selectedRhyme;
    let tooltipText;
    let rhymesListElem;

    const rhymesPreloaded = {};

    //TODO: set correct word pos

    const store = new PoemDataStore(stanzas);

    store.subscribe(value => {
        stanzas = value;
    });

    store.subscribeStanzaIndex(value => {
        stanzaIndex = value;
    });

    store.subscribeLineIndex(value => {
        lineIndex = value;
    });

    store.subscribeWordIndex(value => {
        console.log(value)
        // console.trace()
        wordIndex = value;
    });

    const onFocus = (s, l, w) => {
        if (chooseWord) {
            // if within this stanza update selection
            if (s === targetStanzaIndex) {
                const wordIndForRhyme = findWordForRhyme(s, l).w;
                chosenLineIndex = l;
                chosenWordIndex = wordIndForRhyme;
            } else { // else update target line to get rhymes to
                targetLineIndex = l;
                findChosenLineIndex(l, store.getStanza(s).length);
                findChosenWordIndex(s);
            }
        }
        store.setIndexes(s, l, w);
    };

    const onRemoveBeforeWord = (e, s, l, w) => {
        store.setIndexes(s, l, w);

        const { word, punct } = e.detail;
        updateWord(word);

        // if first word
        if (w === 0) {
            if (l > 0) { // if not first line merge lines
                mergeLines(s, l);
            } else if (s > 0) { // if first line merge stanzas
                mergeStanzas(s);
            }
        } else if (!punct) { // merge words if not punctuation
            mergeWords(s, l, w);
        }
    };

    const onRemoveAfterWord = (s, l, w) => {
        store.setIndexes(s, l, w);
        if (store.isLastLineInStanza(l, s)) {
            mergeStanzas(s + 1);
        }
    };

    const onRemoveWord = (s, l, w) => {
        if (chooseWord || rhymesGot) {
            return;
        }

        store.setIndexes(s, l, w);
        const words = store.getLine();

        store.removeWord();

        if (!words.length) {
            onRemoveLine(s, l);
        } else if (wordIndex === words.length) { //???
            onBackWord(s, l, w);
        }
    };

    const onRemoveLine = (s, l) => {
        if (chooseWord || rhymesGot) {
            return;
        }
        store.setIndexes(s, l);
        store.removeLine();
        if (!store.getStanza().length) {
            store.removeStanza();
        }
    };

    const onEnter = (e, s, l, w) => {
        store.setIndexes(s, l, w);

        const { word, pos } = e.detail;
        store.setWord(word, w, l, s);

        //if enter after word
        if (pos === word.length) {
            // if the last word in line insert line after
            if (store.isLastWordInLine(w, l, s)) {
                onAddLine(s, l, [ '' ]);
            } else {
                // insert line, this word in first part
                splitLine(s, l, w + 1);
            }
        } else if (pos === 0) { // if enter before word
            // if first word insert line before
            if (w === 0) {
                onAddLine(s, l - 1, [ '' ]);
            } else {
                // insert line, this word in second part
                splitLine(s, l, w);
            }
        } else { // enter within word - split word into 2, then split line into 2
            splitWord(s, l, w, pos);
            splitLine(s, l, w + 1);
        }
    };

    const onSpace = (e, s, l, w) => {
        store.setIndexes(s, l, w);
        const { word, pos } = e.detail;
        updateWord(word);

        if (!word) {
            return;
        }
        if (pos === word.length) {
            // insert word after this
            store.addWord('');
        } else if (pos === 0) {
            // insert word before this
            store.addWord('', w - 1);
        } else { // space within word - split into 2
            splitWord(s, l, w, pos);
        }
    };

    const onPunct = (e, s, l, w) => {
        store.setIndexes(s, l, w);
        const word = e.detail.word;
        updateWord(word);
        store.addWord(e.detail.sign);
        store.addWord('');
    };

    const onEditFinished = (e, s, l, w) => {
        const { word } = e.detail;
        store.setIndexes(s, l, w);
        updateWord(word);
    };

    const onNextWord = (s, l, w, e) => {
        store.setIndexes(s, l, w);
        if (!store.isLastWordInLine(w, l, s)) {
            store.setWordIndex(++w);
        } else {
            onNextLine(s, l, w);
        }
        wordPos = 0;
    };

    const onNextLine = (s, l, w) => {
        store.setIndexes(s, l, w);
        if (!store.isLastLineInStanza(l, s)) {
            store.setLineIndex(++l);
            store.setWordIndex(0);
        } else {
            onNextStanza(s, l, w);
        }
        wordPos = 0;
    };

    const onNextStanza = (s, l, w) => {
        store.setIndexes(s, l, w);
        if (s < store.getSize() - 1) {
            store.setStanzaIndex(++s);
            store.setLineIndex(0);
            store.setWordIndex(0);
            wordPos = 0;
        }
    };

    const onBackWord = (s, l, w) => {
        store.setIndexes(s, l, w);
        if (w > 0) {
            store.setWordIndex(--w);
        } else {
            onBackLine(s, l, w);
        }
        wordPos = store.getWord().length;
    };

    const onBackLine = (s, l, w) => {
        store.setIndexes(s, l, w);
        if (l > 0) {
            store.setLineIndex(--l);
            store.setWordIndex(store.getLineSize() - 1);
            wordPos = store.getWord().length;
            stanzas = store.getStanzas();
        } else {
            onBackStanza(s, l, w);
        }
    };

    const onBackStanza = (s, l, w) => {
        store.setIndexes(s, l, w);
        if (s > 0) {
            store.setStanzaIndex(--s);
            store.setLineIndex(store.getStanzaSize() - 1);
            store.setWordIndex(store.getLineSize() - 1);
            wordPos = store.getWord().length;
        }
    };

    // TODO: debounce
    const onUp = (s, l, w) => {
        store.setIndexes(s, l, w);
        onBackLine(s, l, w);
    };

    const onDown = (s, l, w) => {
        store.setIndexes(s, l, w);
        onNextLine(s, l, w);
    };

    const onAddStanza = (lines, s) => {
        store.setIndexes(s);
        const stanza = store.getStanza(s > 0 ? s - 1 : 0);
        if (stanza && stanza.length && stanza[0]) {
            store.addStanza(lines, s);
        }
    };

    const updateWord = (word, w = wordIndex, l = lineIndex, s = stanzaIndex) => {
        if (word) {
            store.setWord(word, w, l, s);
        } else {
            store.removeWord();
            if (w > 0) {
                store.setWordIndex(--w);
            }
        }
    };

    // change args order
    const onAddLine = (s = stanzaIndex, l, newLine) => {
        if (chooseWord || rhymesGot) {
            return;
        }

        store.setIndexes(s, l);
        const line = store.getLine();
        const isNewEmpty = store.isLineEmpty(newLine);

        // if current line is empty
        if (l >= 0 && store.isLineEmpty(line)) {
            // remove this line
            if (isNewEmpty) {
                store.removeLine();
            }

            if (!store.getStanza(s).length) { // if stanza is empty, remove it
                store.removeStanza(s);
                store.setStanzaIndex(stanzaIndex + 1);
                store.setLineIndex(0);
            } else if (l === store.getStanza().length) { // if the last line, add stanza
                store.setLineIndex(0);
                onAddStanza([ [ '' ] ], s);
            } else if (l === 0) {  // if the first line, add stanza
                onAddStanza([ [ '' ] ], s - 1);
            } else if (isNewEmpty) { // if new line is also empty: 2 empty lines => new stanza
                splitStanza(s, l);
            }
        } else {
            store.addLine(isNewEmpty ? [ '' ] : newLine, l, s);
            store.setWordIndex(0);
        }
    };

    const splitStanza = (s, l) => {
        const stanza = store.getStanza(s);
        const stanza1 = stanza.slice(0, l);
        const stanza2 = stanza.slice(l);
        onAddStanza(stanza1, s);
        onAddStanza([ [ '' ] ], s + 1);  // bad pattern
        onAddStanza(stanza2, s + 2);
        store.removeStanza(s);
        store.setIndexes(s + 1, 0, 0);
    };

    const mergeStanzas = s => {
        const stanza1 = store.getStanza(s - 1);
        const stanza2 = store.getStanza(s);
        const stanza = stanza1.concat(stanza2);
        onAddStanza(stanza, s - 1);
        store.removeStanza(s - 1);
        store.removeStanza(s);
        store.setIndexes(s - 1, stanza1.length, 0);
    };

    const splitLine = (s, l, w) => {
        const line = store.getLine(l, s);
        const line1 = line.slice(0, w);
        const line2 = line.slice(w, line.length ? line.length : 1);
        onAddLine(s, l, line1);
        onAddLine(s, l + 1, line2);
        store.removeLine(l);
        store.setIndexes(s, l + 1, 0);
    };

    const mergeLines = (s, l) => {
        let line1 = store.getLine(l - 1, s);
        let line2 = store.getLine(l, s);
        let line = line1.concat(line2);
        if (store.isLineEmpty(line1)) {
            line = line2;
            line1 = [];
        } else if (store.isLineEmpty(line2)) {
            line = line1;
        }
        store.removeLine(l - 1, s);
        store.removeLine(l - 1, s);
        onAddLine(s, l - 2, line);
        store.setIndexes(s, l - 1, line1.length);
    };

    const splitWord = (s, l, w, pos) => {
        const word = store.getWord(w, l, s);
        const word1 = word.slice(0, pos);
        const word2 = word.slice(pos, word.length ? word.length : 1);
        store.addWord(word1, w, l, s);
        store.addWord(word2, w + 1, l, s);
        store.removeWord(w);
        store.setIndexes(s, l, w + 1);
        console.log(word1, word2, s, l, w + 1)
        wordPos = word2.length;
    };

    const mergeWords = (s, l, w) => {
        const word1 = store.getWord(w - 1, l, s);
        const word2 = store.getWord(w, l, s);
        const word = word1.concat(word2);
        store.addWord(word, w, l, s);
        store.removeWord(w, l, s);
        store.removeWord(w - 1, l, s);
        store.setIndexes(s, l, w - 1);
    };

    const onGetRhymes = async (stressPos) => {
        const word = store.getWord(chosenWordIndex, chosenLineIndex, stanzaIndex);
        let response = await getRhymes(word, stressPos);
        if (response.data) {
            rhymes = response.data;
            rhymesGot = true;
            chooseWord = false;
            if (rhymes.length) {
                selectedRhyme = rhymes[0].toLowerCase();
            }
        } else if (response.error === 'Stress needed') {
            chooseWord = false;
            chooseStress = true;
        } else {
            console.log("Ошибка: " + response.status);
        }
    };

    const findChosenLineIndex = (l = lineIndex, stanzaLength = store.getStanza().length) => {
        // get line to get rhyme to
        if (l > 1) {
            chosenLineIndex = l - 2;
        } else if (l > 0) {
            chosenLineIndex = l - 1;
        } else if (l < stanzaLength - 2) {
            chosenLineIndex = l + 2;
        } else if (l < stanzaLength - 1) {
            chosenLineIndex = l + 1;
        }
        // TODO: finish odd logic
        // // for even lines
        // if (l % 2 === 0) {
        //     // if there are previous lines, go 2 lines upper
        //     if (l > 1) {
        //         chosenLineIndex = l - 2;
        //     } else if (stanzaLength === 1) { // if the first line and only 2 lines, go to the second
        //         chosenLineIndex = l - 1;
        //     } else {} // if the first line and only 1 line, nothing to do
        // } else {  // for odd lines
        //
        // }
    };

    const findChosenWordIndex = (s = stanzaIndex) => {
        let w = store.getLine(chosenLineIndex, s).length;
        let l = chosenLineIndex;

        const lw = findWordForRhyme(s, l, w);

        chosenLineIndex = lw.l;
        chosenWordIndex = lw.w;
    };

    const findWordForRhyme = (s, l, w) => {
        let isPunct = false;

        if (!w) {
            w = store.getLine(l, s).length;
        }

        // find word
        do {
            const word = store.getWord(--w, l, s);
            isPunct = isPunctuationMark(word);

            // if there's no words in line, go to previous line
            if (w < 0) {
                w = 0;
                // if the first line in stanza skip
                if (l === 0) {
                    return;
                }
                l--;
            }
        } while (isPunct && w > 0);

        return {
            w, l
        };
    };

    const onChooseBefore = (s, l, w) => {
        if (l > 0) {
            onBackLine(s, l, w);
            chosenLineIndex = lineIndex;
            findChosenWordIndex();
        }
    };

    const onChooseAfter = (s, l, w) => {
        if (!store.isLastLineInStanza(l, s)) {
            onNextLine(s, l, w);
            chosenLineIndex = lineIndex;
            findChosenWordIndex();
        }
    };

    const onBulbClick = (s, l) => {
        if (!rhymesGot && store.getLastWord(l, s)) {
            store.setIndexes(s, l);
            chooseWord = !(chooseWord && l === targetLineIndex);
            wordPos = 0;
            targetStanzaIndex = s;
            targetLineIndex = l;

            // set chosen rhyme line
            findChosenLineIndex();
            findChosenWordIndex();
        }
    };

    const onClickOutsideStanza = (s) => {
        if (s === targetStanzaIndex) {
            resetChoice();
        }
    };

    const onLineClick = (s, l) => {
        if (chooseWord) {
            const wordIndForRhyme = findWordForRhyme(s, l).w;
            chosenLineIndex = l;
            chosenWordIndex = wordIndForRhyme;
        }
        if (!isChoosing()) {
            console.log('clk line')
            store.setStanzaIndex(s);
            store.setLineIndex(l);
            store.setWordIndex(store.getLineSize(l) - 1);
        }
    }

    const onConfirmRhymes = () => {
        // TODO: move to states machine-like maybe??
        console.log('confirm poem', rhymesGot)
        if (rhymesGot) {
            store.addWord(selectedRhyme, store.getLineSize(targetLineIndex) - 1, targetLineIndex);
            store.setIndexes(stanzaIndex, targetLineIndex, -1);
            resetChoice();
        } else if (chooseStress) {
            onGetRhymes(wordPos);
        } else {
            onGetRhymes();
        }
        chooseStress = false;
        selectedRhyme = '';
    };

    const onRejectRhymes = () => {
        resetChoice();
    };

    const resetChoice = () => {
        chooseWord = false;
        wordPos = 0;
        rhymesGot = false;
        rhymes = [];
        chooseStress = false;
        selectedRhyme = '';
        targetStanzaIndex = -1;
    };

    $: isLineChosenForRhymes = (s, l) => {
        return isChoosing() && s === targetStanzaIndex && l === targetLineIndex;
    };

    $: isWordChosenForRhymes = (s, l, w) => {
        return (chooseWord || chooseStress) && s === targetStanzaIndex && l === chosenLineIndex && w === chosenWordIndex;
    };

    $: {
        if (rhymesGot) {
            if (rhymes.length) {
                tooltipText = chooseRhymeTooltip;
            } else {
                tooltipText = noRhymesTooltip;
            }
        } else {
            if (chooseStress) {
                tooltipText = chooseStressTooltip;
            } else {
                tooltipText = chooseTargetTooltip;
            }
        }
    }

    const onKeyDown = (e) => {
        if (isChoosing()) {
            if (e.key === 'Escape') {
                onRejectRhymes();
            } else  if (e.key === 'Enter') {
                onConfirmRhymes();
            }
        }
    };

    const isChoosing = () => {
        return chooseWord || rhymesGot || chooseStress;
    };

    $: if (rhymesListElem) {
        rhymesListElem.focus();
    }
</script>

<div class="poem">
    <h2 contenteditable>
        {title}
    </h2>
    {#each stanzas as lines, s}
        <div class="stanza"
             use:clickOutside
             on:clickOutside={() => onClickOutsideStanza(s)}
             on:keydown={onKeyDown}>
            {#each lines as words, l}
                <div class="line" on:click|preventDefault={() => onLineClick(s, l)}>
                    <div class="words">
                        {#each words as word, w}
                            <Word
                                    {word}
                                    bind:pos={wordPos}
                                    chosen={isWordChosenForRhymes(s, l, w)}
                                    focused={!isChoosing() && s === stanzaIndex && l === lineIndex && w === wordIndex}
                                    editable={!(isChoosing()) && (!isWordChosenForRhymes(s, l, w))}
                                    {chooseStress}
                                    on:focus={() => onFocus(s, l, w)}
                                    on:remove={() => onRemoveWord(s, l, w)}
                                    on:remove-before={e => onRemoveBeforeWord(e, s, l, w)}
                                    on:remove-after={e => onRemoveAfterWord(e, s, l, w)}
                                    on:enter={e => onEnter(e, s, l, w)}
                                    on:space={e => onSpace(e, s, l, w)}
                                    on:punct={e => onPunct(e, s, l, w)}
                                    on:back={e => onBackWord(s, l, w, e)}
                                    on:next={e => onNextWord(s, l, w, e)}
                                    on:up={e => onUp(s, l, w, e)}
                                    on:down={e => onDown(s, l, w, e)}
                                    on:choose-back={() => onChooseBefore(s, l, w)}
                                    on:choose-next={() => onChooseAfter(s, l, w)}
                                    on:choose-up={() => onChooseBefore(s, l, w)}
                                    on:choose-down={() => onChooseAfter(s, l, w)}
                                    on:edit-finished={e => onEditFinished(e, s, l, w)}/>
                        {/each}
                        {#if rhymesGot && l === targetLineIndex && s === stanzaIndex}
                            <select bind:value={selectedRhyme} bind:this={rhymesListElem}>
                                {#if rhymes.length}
                                    {#each rhymes as rhyme}
                                        <option>{rhyme.toLowerCase()}</option>
                                    {/each}
                                {:else}
                                    <option>{noRhymes}</option>
                                {/if}
                            </select>
                        {/if}
                    </div>
                    <div class="right-menu">
                        <div class="bulb-container">
                            <div class="menu-button" class:active={isLineChosenForRhymes(s, l)}>
                                <IconButton
                                        icon="bulb" iconSize="18" padding="4"
                                        opacity={isLineChosenForRhymes(s, l) ? 1: 0.2}
                                        title={getRhymeTitle}
                                        on:click={() => onBulbClick(s, l)}/>
                            </div>
                        </div>
                        <Tooltip
                                open={isLineChosenForRhymes(s, l)}
                                text={tooltipText}
                                showConfirm={!rhymesGot || rhymes.length}
                                on:reject={onRejectRhymes}
                                on:confirm={onConfirmRhymes} />
                        <div class="menu-button">
                            <IconButton icon="cross" iconSize="18" padding="4" title={removeLineTitle}
                                        on:click={() => onRemoveLine(s, l)}/>
                        </div>
                        <div class="menu-button">
                            <IconButton icon="plus" iconSize="18" padding="4" title={addLineTitle}
                                        on:click={() => onAddLine(s, l)}/>
                        </div>
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
    width: -moz-max-content;
    flex-direction: column;
    margin-left: auto;
    margin-right: auto;
    padding: 1rem;
  }

  .stanza {
    margin-bottom: 1rem;
    padding: 0.5rem;

    &:hover, &:focus-within {
      background-color: #F5F5F5;
    }
  }

  .line {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 2rem;

    & .words {
      display: flex;
      align-items: center;
      padding: 0 0.5rem;
      height: 100%;
    }

    .right-menu {
      position: relative;
      display: flex;
      align-items: center;
      float: right;
      margin-left: 1rem;

      & .menu-button {
        opacity: 0;

        &.active {
          opacity: 1;
        }
      }
    }

    &:hover, &:focus, &:focus-within {
      background-color: #E5E5E5;

      .right-menu {
        & .menu-button {
          opacity: 1;
        }
      }
    }
  }

  .bulb-container {
    &:hover + .tooltip {
      display: block;
    }
  }

  h2 {
    margin-left: 2.25rem;
  }
</style>