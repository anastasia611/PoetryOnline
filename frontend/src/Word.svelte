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

    let orig = word;

    $: chosenPos = pos;

    const setPos = (newPos, allowAfter = true) => {
        console.trace()
        console.log('setcp', pos, newPos, word)
        if (newPos < 0) {
            newPos = 0;
        } else {
            const maxVal = allowAfter ? word.length : word.length - 1;
            if (newPos > maxVal) {
                newPos = maxVal;
            }
        }
        pos = newPos;
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
        console.log('key press', old, word, getCaretCharacterOffsetWithin(wordElement))

        if (chosen) {
            e.preventDefault();
        } else {
            setPos(getCaretCharacterOffsetWithin(wordElement) + 1);

            if (!isLetter(e.key) || isPunctuationMark(word)) {
                e.preventDefault();
            }

            if (e.key === 'Enter') {
                dispatch('enter', { word, pos });
            } else if (isPunctuationMark(e.key)) {
                dispatch('punct', { word, sign: e.key });
            } else if (e.key === ' ') {
                if (word.length === 1) {
                    word = word.replace('-', '—');
                }
                dispatch('space', { word, pos });
            }
        }
    };

    const onKeyDown = (e) => {
        const old = word
        word = getWordContent();
        console.log('key down', old, word, focused)

        if (e.key === ' ') {
            e.preventDefault();
        }

        if (chooseStress) {
            e.preventDefault();

            if (e.key === 'ArrowLeft') {
                setPos(pos - 1, false);
            } else if (e.key === 'ArrowRight') {
                setPos(pos + 1, false);
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
            setPos(getCaretCharacterOffsetWithin(wordElement));

            if (e.key === ' ') {
                if (word.length === 1) {
                    console.log('content repl', old, word)
                    word = word.replace('-', '—');
                }
                dispatch('space', { word, pos });
            } else if (e.key === 'ArrowLeft' && pos === 0) {
                e.preventDefault();
                dispatch('back');
            } else if (e.key === 'ArrowRight' && pos === word.length) {
                e.preventDefault();
                dispatch('next');
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                dispatch('up');
            } else if (e.key === 'ArrowDown') {
                e.preventDefault();
                dispatch('down');
            } else if (e.key === 'Backspace' || e.key === 'Delete') {
                console.log('DEL', word, word.length)
                if (!word.length) {
                    // to prevent deletion of symbol in next word
                    console.log('DEL empty')
                    e.preventDefault();
                    onRemove();
                } else if (e.key === 'Backspace' && pos === 0) {
                    console.log('DEL before', word)
                    e.preventDefault();
                    onRemoveBefore();
                } else if (e.key === 'Delete' && pos === word.length) {
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
        if (!chooseStress) {
            setPos(getCaretCharacterOffsetWithin(wordElement));
        }
    };

    const onFocus = () => {
        dispatch('focus');
        focused = true;
    };

    const onBlur = () => {
        dispatch('edit-finished', { word });

        wordElement.textContent = word;
        tabFocused = false;
        focused = false;
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
        setPos(p, false);
    };

    $: if (focused && wordElement) {
        console.log('focused', word)
        wordElement.focus();
    }

    afterUpdate(() => {
        if (editable && wordElement) {
            const old = wordElement.textContent
            wordElement.textContent = word;

            if (focused) {
                wordElement.focus();
                const currPos = getCaretCharacterOffsetWithin(wordElement);
                console.log('set car pos', old, word, pos, currPos, wordElement.textContent)
                if (pos <= word.length && currPos !== pos) {
                    console.log('set12')
                    setCaretPosition(wordElement, pos);
                }
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
            {#if chosen && chooseStress}
                {#each word as letter, i}
                    {#if i === chosenPos}
                        <b>{letter}</b>
                    {:else}
                        <span class="letter" on:click={() => onClickLetter(i)}>
                            {letter}
                        </span>
                    {/if}
                {/each}
            {:else}
                {word}
            {/if}
        </div>
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