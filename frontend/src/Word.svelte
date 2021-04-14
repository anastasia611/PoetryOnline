<script>
    import RemoveButton from "./IconButton.svelte";
    import { createEventDispatcher, onDestroy, onMount } from 'svelte';
    import { isLetter, isPunctuationMark } from "./common/strings";
    import tabFocus from "./actions/tabFocus";
    import { getCaretCharacterOffsetWithin } from "./common/dom";

    export let word = "";
    export let editable = false;

    const title = 'Удалить слово';

    let focused = false;
    let wordElement;
    let pos = 0;

    const dispatch = createEventDispatcher();


    const onRemove = () => {
        dispatch('removeWord');
    };

    const onKeyPress = e => {
        if (!isLetter(e.key) && !isPunctuationMark(e.key) && e.key !== ' ') {
            e.preventDefault();
        }

        if (e.key === 'Enter') {
            console.log('disp enter', word)
            dispatch('enter', { word });
        } else if (isPunctuationMark(e.key)) {
            dispatch('punct', { word, sign: e.key });
        } else if (e.key === ' ') {
            dispatch('space', { word });
        }

        word = getWordContent();
        console.log(e.key, word)
    };

    const onKeyDown = (e) => {
        const pos = getCaretCharacterOffsetWithin(e.target);
        word = getWordContent();
        console.log(e.key, word, word.length, pos)

        if (e.key === 'ArrowLeft' && pos === 0) {
            dispatch('back');
        } else if (e.key === 'ArrowRight' && pos === word.length) {
            dispatch('next');
        } else if (e.key === 'Backspace' || e.key === 'Delete') {
            if (!word.length) {
                console.log('len 1', word)
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
            console.log('rem bl')
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

    onMount(async () => {
        wordElement.addEventListener('keypress', onKeyPress);
        if (editable) {
            console.log('focus', wordElement, word)
            wordElement.focus();
        }
        //let input = document.querySelectorAll('input.word-text'),
        let input = [wordElement],
            buffer = [];
        for (let i = 0; input.length > i; i++) {
            console.log(input[i].value);
            buffer[i] = document.createElement('div');
            buffer[i].style.visibility = "hidden";
            buffer[i].style.position = "absolute";
            //вставляем скрытый div.buffer
            input[i].parentNode.insertBefore(buffer[i], input[i].nextSibling);

            buffer[i].innerHTML = word;
            console.log('set', word)
            input[i].style.width = buffer[i].clientWidth + 16 + 'px';

            input[i].oninput = function() {
                this.nextElementSibling.innerHTML = this.value;
                console.log(this.value, this.nextElementSibling.clientWidth)
                this.style.width = this.nextElementSibling.clientWidth + 16 + 'px';
            };
        }
    });

    $: if (wordElement) {
        console.log('create', wordElement, word)
        wordElement.nextSibling.innerHTML = word;
        wordElement.style.width = wordElement.nextSibling.clientWidth + 16 + 'px';
// wordElement.innerHTML = word;
    }

    $: {
        console.log(word)
    }

</script>


<div class="word" tabindex="-1"
     class:key-focus-visible={focused}
     use:tabFocus
     on:tabfocus={() => {console.log('FOC');focused = true}}
     on:click={onFocus}
     on:focus={onFocus}
     on:keydown={onKeyDown}
     on:blur={onBlur}>

    <input
            contenteditable
            title={word}
            on:click={onFocus}
            value={word}
            style="min-width:16px"
            bind:this={wordElement}
            class="word-text"

    />


    <span class="remove">
        <RemoveButton icon="cross" size="10" {title} on:click={onRemove}/>
    </span>
</div>

<style lang="scss">
    .word {
        display: inline-block;
        width: fit-content;
        //height: 1.5rem;
        padding: 0.5rem;
        line-height: 1rem;
        color: #222;
        cursor: default;
        border-radius: 0.125rem;
        background-clip: padding-box;
        user-select: none;

        & .remove {
            opacity: 0;
        }

        &:hover, &:focus, &:focus-within {
            background-color: #dddddd;
            outline: none;

            & .remove {
                opacity: 1;
            }
        }

        &.key-focus-visible {
            background-color: gray;
            border: #666666 2px solid;
        }
    }

    .word-text {
        color: #500808;
        border: none;
        font-size: 1rem;
        line-height: 1rem;

        &:focus {
            outline: none;
        }
    }

    .buffer {
        position: absolute;
        top: -1000px;
        left: -1000px;
        visibility: hidden;
        white-space: nowrap;
    }

</style>