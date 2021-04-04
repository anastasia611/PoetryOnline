<script>
    import Word from "./Word.svelte";
    import {debounce} from "./common/utils";
    import {isLetter, isPunctuationMark} from "./common/strings";

    export let words = [];

    let newWord = '';

    const onKeyPress = e => {
        if (e.key === "Enter" || isPunctuationMark(e.key)) {
            setTimeout(() => {
                if (newWord) {
                    words.push(newWord);
                }
                if (isPunctuationMark(e.key)) {
                    words.push(e.key);
                }
                words = words;
                newWord = '';
            }, 100);
        } else if (isLetter(e.key)) {
            newWord += e.key;
        } else if (e.key === 'Backspace' || e.key === 'Delete') {
            newWord = newWord.substr(0, newWord.length - 1);
        } else if (e.key === 'ArrowLeft') {
            const pos = e.target.selectionStart - 1;
            e.target.setSelectionRange(pos, pos);
        } else if (e.key === 'ArrowRight') {
            const pos = e.target.selectionStart + 1;
            e.target.setSelectionRange(pos, pos);
        }
    };

    const removeWord = i => {
        setTimeout(() => {
            words.splice(i, 1);
            words = words;
        }, 150);
    };
</script>

<main>
    <h1>Стихотворение</h1>
    <div class="container" id="container1">
        <div class="words">
            {#each words as word, i}
                <Word {word} on:removeWord={() => removeWord(i)}/>
            {/each}
            <div class="word-input">
                <label for="word"></label>
                <input
                        type="text" autocomplete="on" autocapitalize="off" spellcheck="true"
                        class="word-input" id="word" maxlength="32" placeholder="введите слово.."
                        on:keydown|preventDefault={onKeyPress}
                        value={newWord}>
            </div>
            <div class="next-line-container">
                <label for="next-line"></label>
                <button id="next-line"></button>
            </div>
        </div>
    </div>
</main>

<style lang="scss">
    main {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 1em;
        margin: 0 auto;
    }

    h1 {
        color: #DE8484;
    }

    .container {
        padding: 0 5rem;
        display: inline-block;
    }

    .words {
        display: inline-block;
        max-width: 100%;
        width: fit-content;
        height: auto;
        padding: 0.5rem;
        position: relative;
        border: 1px solid #ddd;
        cursor: text;
        background-color: #fff;
        list-style: none;
    }

    .word-input {
        display: inline-block;

        & input {
            height: 2rem;
            margin-left: 0.5rem;
            border: 1px solid white;

            &:focus {
                outline: none;
                border: 1px solid #cccccc;
            }
        }
    }

    .next-line-container {
        display: inline-block;
        float: right;

        #next-line {
            width: 1.5rem;
            height: 1.5rem;
        }
    }

</style>