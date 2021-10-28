<script>
    import RemoveButton from "./IconButton.svelte";
    import { createEventDispatcher, afterUpdate } from 'svelte';
    import { isLetter, isPunctuationMark } from "./common/strings";
    import tabFocus from "./actions/tabFocus";
    import { getCaretCharacterOffsetWithin, setCaretPosition } from "./common/dom";

    export let word = '';
    export let focused = false;
    export let pos = 0;
    export let chooseStress = false;
    export let chosen = false;
    export let editable = false;

    const title = 'Удалить слово';
    const dispatch = createEventDispatcher();

    let tabFocused = false;
    let wordElement;
    let lettersElement;

    let oldPos = pos;
    let curPos = pos;

    const setPos = (newPos, allowAfter = true) => {
        // console.trace()
        console.log('setcp', curPos, newPos, word)
        // why large numbers??
        if (newPos > word.length) {
            return;
        }
        if (newPos < 0) {
            newPos = 0;
        } else {
            const maxVal = allowAfter ? word.length : word.length - 1;
            if (newPos > maxVal) {
                newPos = maxVal;
            }
        }
        curPos = newPos;
        if (chooseStress) {
            pos = curPos;
        }
    };

    const onRemove = () => {
        dispatch('remove');
    };

    const onRemoveBefore = () => {
        dispatch('remove-before', {
            word,
            punct: isPunctuationMark(word)
        });
    };

    const onRemoveAfter = () => {
        dispatch('remove-after', { word });
    };

    const onKeyPress = e => {
        const old = word
        word = getWordContent();

        if (chosen) {
            e.preventDefault();
        } else {
            setPos(getCaretCharacterOffsetWithin(wordElement));

            if (!isLetter(e.key) || isPunctuationMark(word)) {
                e.preventDefault();
            }

            if (e.key === 'Enter') {
                dispatch('enter', { word, pos: curPos });
            } else if (isPunctuationMark(e.key)) {
                dispatch('punct', { word, sign: e.key });
            } else if (e.key === ' ') {
                if (word.length === 1) {
                    word = word.replace('-', '—');
                }
                dispatch('space', { word, pos: curPos });
            }
        }
    };

    const onKeyDown = (e) => {
        console.log('key d', e.key, curPos)
        const old = word
        word = getWordContent();

        if (e.key === ' ') {
            e.preventDefault();
        }

        if (chooseStress) {
            e.preventDefault();

            if (e.key === 'ArrowLeft') {
                setPos(curPos - 1, false);
            } else if (e.key === 'ArrowRight') {
                setPos(curPos + 1, false);
            }
        } else if (chosen) {
            e.preventDefault();

            if (e.key === 'ArrowLeft') {
                dispatch('choose-back');
            } else if (e.key === 'ArrowRight') {
                dispatch('choose-next');
            } else if (e.key === 'ArrowUp') {
                dispatch('choose-back');
            } else if (e.key === 'ArrowDown') {
                dispatch('choose-next');
            }
        } else if (focused) {
            // console.log('key down', getCaretCharacterOffsetWithin(wordElement), word)
            setPos(getCaretCharacterOffsetWithin(wordElement));

            if (e.key === ' ') {
                if (word.length === 1) {
                    console.log('content repl', old, word)
                    word = word.replace('-', '—');
                }
                dispatch('space', { word, pos: curPos });
            } else if (e.key === 'ArrowLeft' && curPos === 0) {
                e.preventDefault();
                dispatch('back');
            } else if (e.key === 'ArrowRight' && curPos === word.length) {
                e.preventDefault();
                dispatch('next');
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                dispatch('up');
            } else if (e.key === 'ArrowDown') {
                e.preventDefault();
                dispatch('down');
            } else if (e.key === 'Backspace' || e.key === 'Delete') {
                console.log('DEL', word, e.key, curPos)
                if (!word.length) {
                    // to prevent deletion of symbol in next word
                    console.log('DEL empty')
                    e.preventDefault();
                    onRemove();
                } else if (e.key === 'Backspace' && curPos === 0) {
                    console.log('DEL before', word)
                    e.preventDefault();
                    onRemoveBefore();
                } else if (e.key === 'Delete' && curPos === word.length) {
                    console.log('DEL after', word)
                    e.preventDefault();
                    onRemoveAfter();
                }
            }
        }
    };

    const getWordContent = () => {
        if (chooseStress || chosen) {
            return word;
        }
        return wordElement.textContent || '';
    };

    const onClick = () => {
        console.log(word, chooseStress)
        if (!chooseStress) {
            setPos(getCaretCharacterOffsetWithin(wordElement));
        }
    };

    const onFocus = () => {
        if (!chosen && !chooseStress) {
            dispatch('focus');
        }
        focused = true;
    };

    const onBlur = () => {
        if (editable) {
            dispatch('edit-finished', { word });
        }

        // may be removed when choosing stress
        if (wordElement) {
            wordElement.textContent = word;
        }
        tabFocused = false;
        focused = false;

        oldPos = -1;
        // console.trace()
        console.log('unfoc')
        // TODO: needed?
        dispatch('blur', { word: getWordContent() });
    };

    const onTabFocus = () => {
        if (!chosen && !chooseStress) {
            tabFocused = true;
        }
    };

    const onInput = () => {
        console.log('input', word, wordElement.textContent, wordElement.textContent.length, pos)
        word = wordElement.textContent;
        // if (pos > 0) {
        //     setPos(pos - 1);
        // }
    };

    const onClickLetter = (p) => {
        console.log('letter', word, p)
        setPos(p, false);
    };

    $: if (focused && wordElement) {
        curPos = pos
        wordElement.focus();
    }

    $: if (chooseStress && lettersElement) {
        lettersElement.focus();
    }

    afterUpdate(() => {
        if (editable && wordElement) {
            const old = wordElement.textContent
            wordElement.textContent = word;

            if (focused) {
                wordElement.focus();
                console.log('set car pos', old, word, oldPos, pos, curPos, wordElement.textContent)
                if (pos <= word.length && pos !== oldPos) {
                    setCaretPosition(wordElement, pos);
                    setPos(pos);
                }
                oldPos = pos;
            }
        }
    });

</script>

<div class="word-container"
     on:click|stopPropagation={onFocus}>
    <div class="word"
         class:chosen={chosen}>

        <div class="remove">
            <RemoveButton icon="cross" disabled={!editable} iconSize="10" {title} on:click={onRemove}/>
        </div>

        {#if chosen && chooseStress}
            <div role="textbox"
                 tabindex=0
                 title={word}
                 class="word-text"
                 class:key-focus-visible={tabFocused}
                 use:tabFocus
                 bind:this={lettersElement}
                 on:keydown={onKeyDown}
                 on:tabfocus={onTabFocus}
                 on:focus={onFocus}
                 on:blur={onBlur}
            >
                {#each word as letter, i}
                    {#if i === curPos}
                        <b>{letter}</b>
                    {:else}
                        <span class="letter" on:click={() => onClickLetter(i)}>
                            {letter}
                        </span>
                    {/if}
                {/each}
            </div>
        {:else}
            <div role="textbox"
                 tabindex=0
                 title={word}
                 contenteditable={editable}
                 class="word-text"
                 on:input={onInput}
                 bind:this={wordElement}
                 class:key-focus-visible={tabFocused}
                 use:tabFocus
                 on:keypress={onKeyPress}
                 on:keydown={onKeyDown}
                 on:click={onClick}
                 on:tabfocus={onTabFocus}
                 on:focus={onFocus}
                 on:blur={onBlur}
            >
                {word}
            </div>
        {/if}
    </div>
</div>

<style lang="scss">
  .word-container {
    display: flex;
    align-items: center;
    //padding: 0.25rem 0.5rem;
    height: 100%;

    &:hover, &:focus, &:focus-within {
      & .word {
        background-color: #D0D0D0;
        outline: none;

        & .remove {
          opacity: 1;
        }
      }
    }
  }

  .word {
    --size: 1rem;

    display: inline-flex;
    padding: 0.2rem 0.125rem;
    line-height: var(--size);
    color: #222;
    cursor: default;
    border-radius: 0.125rem;
    background-clip: padding-box;
    user-select: none;

    & .remove {
      display: flex;
      opacity: 0;
      margin-right: 0.125rem;
    }

    &:hover, &:focus, &:focus-within {
      background-color: #D0D0D0;
      outline: none;

      & .remove {
        opacity: 1;
      }
    }

    &.chosen {
      box-shadow: 0 0 0.25rem 0.25rem #500808AA;
      //outline: #500808AA 0.125rem solid;
    }
  }

  .word-text {
    display: inline;
    color: #500808;
    border: none;
    font-size: var(--size);
    line-height: var(--size);
    min-width: var(--size);
    background: none;
    padding-right: 0.125rem;

    &:focus {
      outline: none;
    }

    &.key-focus-visible {
      outline: #888888 0.125rem solid;
    }
  }

  .letter {
    display: inline-block;
    width: fit-content;
  }
</style>