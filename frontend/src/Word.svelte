<script>
    import RemoveButton from "./IconButton.svelte";
    import { createEventDispatcher, onDestroy, onMount } from 'svelte';
    import { isLetter, isPunctuationMark } from "./common/strings";
    import tabFocus from "./actions/tabFocus";
    import { convertPixelsToRem } from "./common/dom";

    export let word = "";
    export let editable = false;

    const title = 'Удалить слово';

    let focused = false;
    let wordElement;
    let hidden;
    let pos = 0;

    const dispatch = createEventDispatcher();


    const onRemove = () => {
        dispatch('removeWord');
    };

    const onKeyPress = e => {
        const pos = e.target.selectionEnd;
        console.log(e.key, word, pos, word.length)

        if (!isLetter(e.key)) {
            e.preventDefault();
        }
        // if (e.key === ' ' && pos !== word.length) {
        //     e.preventDefault();
        // }

        if (e.key === 'Enter') {
            console.log('disp enter', word)
            dispatch('enter', { word });
        } else if (isPunctuationMark(e.key)) {
            dispatch('punct', { word, sign: e.key });
        } else if (e.key === ' ') {
            if (pos !== word.length) {
                e.preventDefault();
                return;
            } else {
                dispatch('space', { word });
            }
        }

        word = getWordContent();
    };

    const onKeyDown = (e) => {
        const pos = e.target.selectionEnd;
        word = getWordContent();
        console.log(e.key, word, pos)

        if (e.key === 'ArrowLeft' && pos === 0) {
            dispatch('back');
        } else if (e.key === 'ArrowRight' && pos === word.length) {
            dispatch('next');
        } else if (e.key === 'Backspace' || e.key === 'Delete') {
            if (!word.length) {
                onRemove();
            }
            if (!pos) {
                dispatch('back');
            }
        }
    };

    const getWordContent = () => {
        // return (wordElement.textContent || '').trim();
        return wordElement.value;
    };

    const onFocus = () => {
        editable = true;
    };

    const onBlur = () => {
        if (!word) {
            onRemove();
        } else {
            dispatch('editFinished', { word });
        }

        editable = false;
    };

    onDestroy(() => {
        if (wordElement) {
            wordElement.removeEventListener('keypress', onKeyPress);
        }
    });

    let width = 0;

    onMount(async () => {
        wordElement.addEventListener('keypress', onKeyPress);
        hidden.innerHTML = word;
    });

    const onInput = () => {
        word = wordElement.value;
        hidden.innerHTML = wordElement.value;
        width = convertPixelsToRem(hidden.clientWidth) + 0.25 + 'rem';
    };

    $: if (wordElement) {
        hidden.innerHTML = word;
        width = convertPixelsToRem(hidden.clientWidth) + 0.25 + 'rem';
    }
    $: if (wordElement && editable) {
        console.log('foc', word)
        wordElement.focus();
    }

</script>

<div class="word" tabindex="-1"
     on:click={onFocus}
     on:focus={onFocus}
     on:keydown={onKeyDown}
     on:blur={onBlur}>

    <input
            title={word}
            class="word-text"
            on:click={onFocus}
            value={word}
            style="width: {width}"
            on:input={onInput}
            bind:this={wordElement}
            class:key-focus-visible={focused}
            use:tabFocus
            on:tabfocus={() => focused = true}
            on:blur={() => focused = false}
    />
    <div class="hidden"
         bind:this={hidden}>
    </div>

    <div class="remove">
        <RemoveButton icon="cross" size="10" {title} on:click={onRemove}/>
    </div>
</div>

<style lang="scss">
    .word {
        display: inline-block;
        padding: 0.25rem 0.25rem;
        line-height: 1rem;
        color: #222;
        cursor: default;
        border-radius: 0.125rem;
        background-clip: padding-box;
        user-select: none;

        & .remove {
            display: inline-block;
            opacity: 0;
        }

        &:hover, &:focus, &:focus-within {
            background-color: #D0D0D0;
            outline: none;

            & .remove {
                opacity: 1;
            }
        }
    }

    .word-text {
        color: #500808;
        border: none;
        font-size: 1rem;
        line-height: 1rem;
        min-width: 1rem;
        //width: var(--w);
        background: none;
        padding: 0;

        &:focus {
            outline: none;
        }

        &.key-focus-visible {
            //background-color: red;
            border: #666666 2px dotted;
        }
    }

    .hidden {
        position: absolute;
        visibility: hidden;
    }

</style>